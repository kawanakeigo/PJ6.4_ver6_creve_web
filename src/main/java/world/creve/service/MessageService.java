package world.creve.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.dto.request.MessageRequest;
import world.creve.dto.response.MessageResponse;
import world.creve.entity.Artwork;
import world.creve.entity.Creator;
import world.creve.entity.Event;
import world.creve.entity.Message;
import world.creve.entity.MessageStatus;
import world.creve.entity.PublishStatus;
import world.creve.exception.BadRequestException;
import world.creve.exception.NotFoundException;
import world.creve.repository.ArtworkRepository;
import world.creve.repository.CreatorRepository;
import world.creve.repository.EventArtworkRepository;
import world.creve.repository.EventCreatorRepository;
import world.creve.repository.EventRepository;
import world.creve.repository.MessageRepository;
import world.creve.security.PostingRateLimiter;
import world.creve.util.PetalLayoutUtil;
import world.creve.util.SessionHashUtil;
import world.creve.validation.MessageValidator;

@Service
public class MessageService {
  private final MessageRepository messageRepository;
  private final EventRepository eventRepository;
  private final CreatorRepository creatorRepository;
  private final ArtworkRepository artworkRepository;
  private final EventCreatorRepository eventCreatorRepository;
  private final EventArtworkRepository eventArtworkRepository;
  private final MessageValidator messageValidator;
  private final PostingRateLimiter postingRateLimiter;
  private final SessionHashUtil sessionHashUtil;
  private final PetalLayoutUtil petalLayoutUtil;

  public MessageService(
      MessageRepository messageRepository,
      EventRepository eventRepository,
      CreatorRepository creatorRepository,
      ArtworkRepository artworkRepository,
      EventCreatorRepository eventCreatorRepository,
      EventArtworkRepository eventArtworkRepository,
      MessageValidator messageValidator,
      PostingRateLimiter postingRateLimiter,
      SessionHashUtil sessionHashUtil,
      PetalLayoutUtil petalLayoutUtil
  ) {
    this.messageRepository = messageRepository;
    this.eventRepository = eventRepository;
    this.creatorRepository = creatorRepository;
    this.artworkRepository = artworkRepository;
    this.eventCreatorRepository = eventCreatorRepository;
    this.eventArtworkRepository = eventArtworkRepository;
    this.messageValidator = messageValidator;
    this.postingRateLimiter = postingRateLimiter;
    this.sessionHashUtil = sessionHashUtil;
    this.petalLayoutUtil = petalLayoutUtil;
  }

  @Transactional
  public MessageResponse registerMessage(MessageRequest request, HttpServletRequest servletRequest) {
    Event event = eventRepository.findById(request.getEventId())
        .orElseThrow(() -> new NotFoundException("イベントが見つかりません。"));
    if (!event.isPublished()) {
      throw new NotFoundException("公開中のイベントが見つかりません。");
    }
    if (!event.isPostable()) {
      throw new BadRequestException("このイベントは現在メッセージ投稿を受け付けていません。");
    }

    String body = request.getBody().trim();
    String displayName = normalizeDisplayName(request.getDisplayName());
    String creatorId = normalize(request.getCreatorId());
    String artworkId = normalize(request.getArtworkId());

    Artwork artwork = resolveArtwork(event.getId(), artworkId);
    if (creatorId == null && artwork != null) {
      creatorId = artwork.getCreatorId();
    }
    Creator creator = resolveCreator(event.getId(), creatorId);
    if (artwork != null && !artwork.getCreatorId().equals(creator.getId())) {
      throw new BadRequestException("作品とクリエイターの組み合わせが正しくありません。");
    }

    MessageStatus status = messageValidator.validateAndDecideStatus(body, displayName);
    String sessionHash = sessionHash(servletRequest);
    OffsetDateTime oneMinuteAgo = OffsetDateTime.now().minusMinutes(1);
    if (messageRepository.countBySessionHashAndCreatedAtAfter(sessionHash, oneMinuteAgo) >= 5) {
      throw new BadRequestException("投稿が集中しています。少し時間をおいてから送信してください。");
    }
    if (messageRepository.existsBySessionHashAndEventIdAndBodyAndCreatedAtAfter(
        sessionHash,
        event.getId(),
        body,
        oneMinuteAgo
    )) {
      throw new BadRequestException("同じメッセージが連続して投稿されています。");
    }
    postingRateLimiter.check(sessionHash, event.getId(), creator.getId(), artworkId);

    long currentCount = messageRepository.countByEventIdAndStatus(
        event.getId(),
        MessageStatus.PUBLISHED
    );
    long petalSeed = petalLayoutUtil.createSeed(
        event.getId(),
        creator.getId(),
        artworkId,
        body,
        sessionHash,
        currentCount + 1
    );
    int growthStage = petalLayoutUtil.calculateGrowthStage(currentCount + 1);
    Message message = new Message(
        event.getId(),
        creator.getId(),
        artworkId,
        body,
        displayName,
        request.isAnonymous(),
        status,
        petalLayoutUtil.petalType(petalSeed),
        petalSeed,
        petalLayoutUtil.x(petalSeed, currentCount, growthStage),
        petalLayoutUtil.y(petalSeed, currentCount, growthStage),
        petalLayoutUtil.angle(petalSeed, currentCount),
        petalLayoutUtil.size(petalSeed, growthStage),
        sessionHash
    );
    return MessageResponse.from(messageRepository.save(message));
  }

  @Transactional
  public MessageResponse create(MessageRequest request, HttpServletRequest servletRequest) {
    return registerMessage(request, servletRequest);
  }

  public List<MessageResponse> findPublicByEvent(String eventId) {
    return messageRepository.findPublishedMessagesByEventId(eventId, MessageStatus.PUBLISHED)
        .stream()
        .map(MessageResponse::from)
        .toList();
  }

  public List<MessageResponse> findPublicByCreator(String eventId, String creatorId) {
    return messageRepository.findPublishedMessagesByCreatorId(
            eventId,
            creatorId,
            MessageStatus.PUBLISHED
        )
        .stream()
        .map(MessageResponse::from)
        .toList();
  }

  public List<MessageResponse> findPublicByArtwork(String eventId, String artworkId) {
    return messageRepository.findPublishedMessagesByArtworkId(
            eventId,
            artworkId,
            MessageStatus.PUBLISHED
        )
        .stream()
        .map(MessageResponse::from)
        .toList();
  }

  public List<Message> findAllForAdmin() {
    return messageRepository.findAllByOrderByCreatedAtDesc();
  }

  @Transactional
  public void publish(Long messageId) {
    Message message = findMessage(messageId);
    message.publish();
  }

  @Transactional
  public void hide(Long messageId) {
    Message message = findMessage(messageId);
    message.hide();
  }

  @Transactional
  public void markDeleted(Long messageId) {
    Message message = findMessage(messageId);
    message.markDeleted();
  }

  public long countPublicByEvent(String eventId) {
    return messageRepository.countByEventIdAndStatus(eventId, MessageStatus.PUBLISHED);
  }

  public long countPublicByCreator(String eventId, String creatorId) {
    return messageRepository.countByEventIdAndCreatorIdAndStatus(
        eventId,
        creatorId,
        MessageStatus.PUBLISHED
    );
  }

  public long countPublicByArtwork(String eventId, String artworkId) {
    return messageRepository.countByEventIdAndArtworkIdAndStatus(
        eventId,
        artworkId,
        MessageStatus.PUBLISHED
    );
  }

  public Message findMessage(Long messageId) {
    return messageRepository.findById(messageId)
        .orElseThrow(() -> new NotFoundException("投稿が見つかりません。"));
  }

  private Creator resolveCreator(String eventId, String creatorId) {
    if (creatorId == null) {
      throw new BadRequestException("クリエイターまたは作品を指定してください。");
    }
    Creator creator = creatorRepository.findById(creatorId)
        .filter(found -> found.getStatus() == PublishStatus.PUBLISHED)
        .orElseThrow(() -> new NotFoundException("クリエイターが見つかりません。"));
    if (!eventCreatorRepository.existsByEventIdAndCreatorId(eventId, creator.getId())) {
      throw new NotFoundException("イベント参加クリエイターが見つかりません。");
    }
    return creator;
  }

  private Artwork resolveArtwork(String eventId, String artworkId) {
    if (artworkId == null) {
      return null;
    }
    Artwork artwork = artworkRepository.findById(artworkId)
        .filter(found -> found.getStatus() == PublishStatus.PUBLISHED)
        .orElseThrow(() -> new NotFoundException("作品が見つかりません。"));
    if (!eventArtworkRepository.existsByEventIdAndArtworkId(eventId, artwork.getId())) {
      throw new NotFoundException("イベント展示作品が見つかりません。");
    }
    return artwork;
  }

  private String normalizeDisplayName(String displayName) {
    String normalized = normalize(displayName);
    return normalized == null ? "匿名" : normalized;
  }

  private String normalize(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }

  private String sessionHash(HttpServletRequest servletRequest) {
    return sessionHashUtil.hash(servletRequest.getSession(true).getId());
  }

}
