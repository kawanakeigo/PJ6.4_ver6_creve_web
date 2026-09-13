package world.creve.playpit.service;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.creve.platform.entity.*;
import world.creve.platform.service.*;
import world.creve.platform.security.*;
import world.creve.platform.exception.*;
import world.creve.playpit.entity.*;
import world.creve.playpit.repository.*;
import world.creve.playpit.dto.request.*;
import world.creve.playpit.dto.response.*;
import world.creve.playpit.validation.MessagePolicy;
import world.creve.playpit.util.PetalLayout;
@Service @Transactional(readOnly=true) public class MessageService {
    private static final Logger log=LoggerFactory.getLogger(MessageService.class);
    private final MessageRepository messages;
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final SubmissionStore submissions;
    private final SessionIdentity identity;
    private final IdentityHasher hasher;
    private final Clock clock;
    private final boolean enabled;
    public MessageService(MessageRepository messages,EventService events,CreatorService creators,ArtworkService artworks,SubmissionStore submissions,SessionIdentity identity,IdentityHasher hasher,Clock clock,@Value("${creve.posting-enabled:false}")boolean enabled) {
        this.messages=messages;
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.submissions=submissions;
        this.identity=identity;
        this.hasher=hasher;
        this.clock=clock;
        this.enabled=enabled;
    }
    @Transactional public MessageResponse registerMessage(MessageRequest request) {
        // Detailed design 19: validate target/participation before content or persistence.
        Event event=events.requirePublished(request.getEventId());
        if(!enabled)throw new ForbiddenException("現在は投稿を受け付けていません。");
        if(!Boolean.TRUE.equals(request.getAgreement())||request.getAnonymous()==null||(request.getCreatorId()==null&&request.getArtworkId()==null))throw new InvalidMessageException();
        Long creatorId=request.getCreatorId();
        if(creatorId!=null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(event.getEventId(),creatorId);
        }
        if(request.getArtworkId()!=null) {
            Artwork artwork=artworks.requirePublished(request.getArtworkId());
            artworks.validateExhibition(event.getEventId(),artwork.getArtworkId());
            if(creatorId!=null&&!creatorId.equals(artwork.getCreatorId()))throw new InvalidParticipationException();
            creatorId=artwork.getCreatorId();
            creators.validateCreatorParticipation(event.getEventId(),creatorId);
        }
        String body=request.getMessage()==null?null:request.getMessage().trim();
        String displayName=request.getDisplayName()==null?null:request.getDisplayName().trim();
        MessageStatus status=MessagePolicy.evaluate(body,displayName);
        LocalDateTime now=LocalDateTime.now(clock);
        String sessionHash=identity.hash(),key=identity.idempotencyKey(),bodyHash=hasher.hash("body:"+body);
        String requestHash=hasher.hash("payload:"+event.getEventId()+":"+creatorId+":"+request.getArtworkId()+":"+body.length()+":"+body+":"+displayName+":"+request.getAnonymous());
        submissions.lock(sessionHash,now);
        Optional<Long> replay=submissions.replay(sessionHash,key,requestHash);
        if(replay.isPresent())return response(messages.findById(replay.get()).orElseThrow(InvalidMessageException::new));
        PostingPolicy.check(messages.countRecentMessagesBySessionHash(sessionHash,now.minus(PostingPolicy.WINDOW)),messages.existsRecentDuplicateMessage(sessionHash,bodyHash,now.minus(PostingPolicy.DUPLICATE_WINDOW)));
        long ordinal=submissions.nextPetalOrdinal(event.getEventId());
        long visible=messages.countPublishedMessagesByEventId(event.getEventId());
        long seed=PetalLayout.seed(ordinal,Math.max(1,PetalLayout.growth(visible+1)));
        Message m=new Message();
        m.setEventId(event.getEventId());
        m.setCreatorId(creatorId);
        m.setArtworkId(request.getArtworkId());
        m.setMessage(body);
        m.setDisplayName(displayName);
        m.setAnonymous(request.getAnonymous());
        m.setStatus(status);
        m.setPetalType(PetalLayout.type(seed));
        m.setPetalSeed(seed);
        m.setCreatedAt(now);
        m.setUpdatedAt(now);
        m=messages.saveAndFlush(m);
        submissions.record(sessionHash,key,requestHash,bodyHash,m.getMessageId(),now);
        submissions.postingAnalytics(event.getEventId(),creatorId,request.getArtworkId(),now);
        log.info("message_registered messageId={} status={}",m.getMessageId(),status);
        return response(m);
    }
    public MessageResponse response(Message m) {
        String text=m.getStatus()==MessageStatus.PUBLISHED?"あなたの言葉が、一枚の花びらになりました。":"投稿を受け付けました。運営の確認後に公開されます。";
        return new MessageResponse(m.getMessageId(),m.getStatus().name(),m.getPetalType(),m.getPetalSeed(),m.getCreatedAt().toString(),text);
    }
    public void validatePublicScope(Long eventId,Long creatorId,Long artworkId) {
        events.requirePublished(eventId);
        if(creatorId!=null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(eventId,creatorId);
        }
        if(artworkId!=null) {
            Artwork a=artworks.requirePublished(artworkId);
            artworks.validateExhibition(eventId,artworkId);
            creators.validateCreatorParticipation(eventId,a.getCreatorId());
            if(creatorId!=null&&!creatorId.equals(a.getCreatorId()))throw new InvalidParticipationException();
        }
    }
    public Page<MessageRow> getPublishedMessages(Long eventId,Long creatorId,Long artworkId,int page,int size) {
        validatePublicScope(eventId,creatorId,artworkId);
        return messages.findFiltered(eventId,creatorId,artworkId,PageRequest.of(Math.max(0,page),Math.min(300,Math.max(1,size))));
    }
    public List<MessageRow> getPublishedMessagesByCreator(Long eventId,Long creatorId) {
        return getPublishedMessages(eventId,creatorId,null,0,30).getContent();
    }
    public List<MessageRow> getPublishedMessagesByArtwork(Long eventId,Long artworkId) {
        return getPublishedMessages(eventId,null,artworkId,0,30).getContent();
    }
    public long countPublishedMessagesByCreator(Long eventId,Long creatorId) {
        validatePublicScope(eventId,creatorId,null);
        return messages.countVisible(eventId,creatorId,null);
    }
    public long countPublishedMessagesByArtwork(Long eventId,Long artworkId) {
        validatePublicScope(eventId,null,artworkId);
        return messages.countVisible(eventId,null,artworkId);
    }
    public long countPublishedMessagesByEventId(Long eventId) {
        validatePublicScope(eventId,null,null);
        return messages.countPublishedMessagesByEventId(eventId);
    }
    public List<CreatorMessageCount> creatorCounts(Long eventId) {
        validatePublicScope(eventId,null,null);
        return messages.creatorCounts(eventId);
    }
    public Page<Message> searchMessages(Long eventId,Long creatorId,Long artworkId,MessageStatus status,LocalDate date,String keyword,int page) {
        Specification<Message> spec=(root,query,cb)-> {
            var predicates=new ArrayList<jakarta.persistence.criteria.Predicate>();
            if(eventId!=null)predicates.add(cb.equal(root.get("eventId"),eventId));
            if(creatorId!=null)predicates.add(cb.equal(root.get("creatorId"),creatorId));
            if(artworkId!=null)predicates.add(cb.equal(root.get("artworkId"),artworkId));
            if(status!=null)predicates.add(cb.equal(root.get("status"),status));
            if(date!=null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),date.atStartOfDay()));
                predicates.add(cb.lessThan(root.get("createdAt"),date.plusDays(1).atStartOfDay()));
            }
            if(keyword!=null&&!keyword.isBlank()) {
                if(keyword.length()>300)throw new InvalidMessageException();
                String escaped=keyword.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_");
                predicates.add(cb.like(cb.lower(root.get("message")),"%"+escaped+"%",'!'));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        }
        ;
        return messages.findAll(spec,PageRequest.of(Math.max(0,page),30,Sort.by(Sort.Direction.DESC,"createdAt")));
    }
    @Transactional public void changeStatus(Long messageId,MessageStatus status) {
        Message m=messages.findById(messageId).orElseThrow(InvalidMessageException::new);
        if(status==null)throw new InvalidMessageException();
        m.setStatus(status);
        messages.save(m);
        log.info("message_status_changed messageId={} status={}",messageId,status);
    }
    @Transactional public void deleteMessage(Long messageId) {
        changeStatus(messageId,MessageStatus.DELETED);
    }
}
