package world.creve.playpit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.entity.Artwork;
import world.creve.platform.exception.ForbiddenException;
import world.creve.platform.exception.InvalidMessageException;
import world.creve.platform.exception.InvalidParticipationException;
import world.creve.platform.security.IdentityHasher;
import world.creve.platform.security.SessionIdentity;
import world.creve.platform.service.ArtworkService;
import world.creve.platform.service.CreatorService;
import world.creve.platform.service.EventService;
import world.creve.playpit.dto.request.MessageRequest;
import world.creve.playpit.dto.response.CreatorMessageCount;
import world.creve.playpit.dto.response.MessageResponse;
import world.creve.playpit.dto.response.MessageRow;
import world.creve.playpit.entity.Message;
import world.creve.playpit.entity.MessageStatus;
import world.creve.playpit.repository.MessageRepository;
import world.creve.playpit.repository.SubmissionStore;
import world.creve.playpit.validation.MessagePolicy;

@Service
@Transactional(readOnly = true)
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private final MessageRepository messages;
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final SubmissionStore submissions;
    private final SessionIdentity identity;
    private final IdentityHasher hasher;
    private final Clock clock;
    private final ObjectMapper json;
    private final boolean enabled;

    public MessageService(
            MessageRepository messages,
            EventService events,
            CreatorService creators,
            ArtworkService artworks,
            SubmissionStore submissions,
            SessionIdentity identity,
            IdentityHasher hasher,
            Clock clock,
            ObjectMapper json,
            @Value("${playpit.posting-enabled:false}") boolean enabled) {
        this.messages = messages;
        this.events = events;
        this.creators = creators;
        this.artworks = artworks;
        this.submissions = submissions;
        this.identity = identity;
        this.hasher = hasher;
        this.clock = clock;
        this.json = json;
        this.enabled = enabled;
    }

    @Transactional
    public MessageResponse registerMessage(MessageRequest request, UUID requestKey) {
        var event = events.requirePublished(request.getEventId());
        if (!enabled || !Boolean.TRUE.equals(event.getAcceptingMessages())) {
            throw new ForbiddenException("現在は投稿を受け付けていません。");
        }
        if (!Boolean.TRUE.equals(request.getAgreement()) || request.getAnonymous() == null) {
            throw new InvalidMessageException();
        }

        Long creatorId = resolveTarget(event.getEventId(), request.getCreatorId(), request.getArtworkId());
        String body = request.getMessage() == null ? "" : request.getMessage().strip();
        if (body.isEmpty() || body.codePointCount(0, body.length()) > 300) {
            throw new InvalidMessageException("感想は1〜300文字で入力してください。");
        }
        String displayName = request.getDisplayName() == null ? null : request.getDisplayName().strip();
        if (displayName != null && displayName.codePointCount(0, displayName.length()) > 30) {
            throw new InvalidMessageException("表示名は30文字以内で入力してください。");
        }
        if (!Boolean.TRUE.equals(request.getAnonymous()) && (displayName == null || displayName.isEmpty())) {
            displayName = "来場者";
        }

        MessageStatus status = MessagePolicy.evaluate(body, displayName);
        LocalDateTime localNow = LocalDateTime.ofInstant(clock.instant(), TOKYO);
        String subjectHash = identity.hash();
        String bodyHash = hasher.hash("body:" + body);
        String requestHash = hasher.hash("payload:" + event.getEventId() + ":" + creatorId + ":" +
                request.getArtworkId() + ":" + body + ":" + displayName + ":" + request.getAnonymous());

        submissions.lock(subjectHash, clock.instant());
        Optional<Long> replay = submissions.replay(subjectHash, requestKey, requestHash, clock.instant());
        if (replay.isPresent()) {
            return response(messages.findById(replay.get()).orElseThrow(InvalidMessageException::new));
        }
        submissions.enforceLimits(subjectHash, bodyHash, clock.instant());

        Message message = new Message();
        message.setEventId(event.getEventId());
        message.setCreatorId(creatorId);
        message.setArtworkId(request.getArtworkId());
        message.setMessage(body);
        message.setDisplayName(displayName);
        message.setAnonymous(request.getAnonymous());
        message.setAgreement(true);
        message.setStatus(status);
        message.setCreatedAt(localNow);
        message.setUpdatedAt(localNow);
        message = messages.saveAndFlush(message);

        MessageResponse response = response(message);
        submissions.record(subjectHash, requestKey, requestHash, bodyHash, message.getMessageId(), toJson(response), clock.instant());
        submissions.messageAcceptedMetric(
                UUID.nameUUIDFromBytes((subjectHash + requestKey).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                event.getEventId(), creatorId, request.getArtworkId(), subjectHash, clock.instant());
        log.info("message_registered messageId={} status={}", message.getMessageId(), status);
        return response;
    }

    public Optional<MessageResponse> findReceipt(UUID requestKey) {
        return submissions.findReceipt(identity.hash(), requestKey, clock.instant())
                .flatMap(messages::findById)
                .map(this::response);
    }

    private Long resolveTarget(Long eventId, Long requestedCreatorId, Long artworkId) {
        if (requestedCreatorId == null && artworkId == null) {
            throw new InvalidMessageException();
        }
        Long creatorId = requestedCreatorId;
        if (creatorId != null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(eventId, creatorId);
        }
        if (artworkId != null) {
            Artwork artwork = artworks.requirePublished(artworkId);
            artworks.validateExhibition(eventId, artworkId);
            if (creatorId != null && !creatorId.equals(artwork.getCreatorId())) {
                throw new InvalidParticipationException();
            }
            creatorId = artwork.getCreatorId();
            creators.validateCreatorParticipation(eventId, creatorId);
        }
        return creatorId;
    }

    public MessageResponse response(Message message) {
        String completion = message.getStatus() == MessageStatus.PUBLISHED
                ? "感想を受け付けました。"
                : "感想を受け付けました。確認後に公開されます。";
        return new MessageResponse(message.getMessageId(), message.getStatus().name(),
                message.getCreatedAt().toString(), completion);
    }

    private String toJson(MessageResponse response) {
        try {
            return json.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("受付結果を保存できません。", exception);
        }
    }

    public void validatePublicScope(Long eventId, Long creatorId, Long artworkId) {
        events.requirePublished(eventId);
        if (creatorId != null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(eventId, creatorId);
        }
        if (artworkId != null) {
            Artwork artwork = artworks.requirePublished(artworkId);
            artworks.validateExhibition(eventId, artworkId);
            creators.validateCreatorParticipation(eventId, artwork.getCreatorId());
            if (creatorId != null && !creatorId.equals(artwork.getCreatorId())) {
                throw new InvalidParticipationException();
            }
        }
    }

    public Page<MessageRow> getPublishedMessages(Long eventId, Long creatorId, Long artworkId, int page, int size) {
        validatePublicScope(eventId, creatorId, artworkId);
        return messages.findFiltered(eventId, creatorId, artworkId,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    public List<MessageRow> getPublishedMessagesByCreator(Long eventId, Long creatorId) {
        return getPublishedMessages(eventId, creatorId, null, 0, 20).getContent();
    }

    public List<MessageRow> getPublishedMessagesByArtwork(Long eventId, Long artworkId) {
        return getPublishedMessages(eventId, null, artworkId, 0, 20).getContent();
    }

    public long countPublishedMessagesByCreator(Long eventId, Long creatorId) {
        validatePublicScope(eventId, creatorId, null);
        return messages.countVisible(eventId, creatorId, null);
    }

    public long countPublishedMessagesByArtwork(Long eventId, Long artworkId) {
        validatePublicScope(eventId, null, artworkId);
        return messages.countVisible(eventId, null, artworkId);
    }

    public long countPublishedMessagesByEventId(Long eventId) {
        validatePublicScope(eventId, null, null);
        return messages.countPublishedMessagesByEventId(eventId);
    }

    public List<CreatorMessageCount> creatorCounts(Long eventId) {
        validatePublicScope(eventId, null, null);
        return messages.creatorCounts(eventId);
    }

    public Page<Message> searchMessages(
            Long eventId, Long creatorId, Long artworkId, MessageStatus status,
            LocalDate date, String keyword, int page) {
        Specification<Message> specification = (root, query, criteria) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (eventId != null) predicates.add(criteria.equal(root.get("eventId"), eventId));
            if (creatorId != null) predicates.add(criteria.equal(root.get("creatorId"), creatorId));
            if (artworkId != null) predicates.add(criteria.equal(root.get("artworkId"), artworkId));
            if (status != null) predicates.add(criteria.equal(root.get("status"), status));
            if (date != null) {
                predicates.add(criteria.greaterThanOrEqualTo(root.get("createdAt"), date.atStartOfDay()));
                predicates.add(criteria.lessThan(root.get("createdAt"), date.plusDays(1).atStartOfDay()));
            }
            if (keyword != null && !keyword.isBlank()) {
                if (keyword.codePointCount(0, keyword.length()) > 100) throw new InvalidMessageException();
                String escaped = keyword.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
                predicates.add(criteria.like(criteria.lower(root.get("message")), "%" + escaped + "%", '!'));
            }
            return criteria.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return messages.findAll(specification,
                PageRequest.of(Math.max(0, page), 20, Sort.by(Sort.Direction.DESC, "createdAt", "messageId")));
    }

    @Transactional
    public void changeStatus(Long messageId, MessageStatus status) {
        Message message = messages.findById(messageId).orElseThrow(InvalidMessageException::new);
        if (status == null) throw new InvalidMessageException();
        message.setStatus(status);
        messages.save(message);
        log.info("message_status_changed messageId={} status={}", messageId, status);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        changeStatus(messageId, MessageStatus.DELETED);
    }
}
