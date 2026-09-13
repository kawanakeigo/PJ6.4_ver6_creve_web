package world.creve.playpit.service;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import world.creve.playpit.repository.MessageRepository;
import world.creve.playpit.entity.MessageStatus;
import world.creve.platform.service.*;
import world.creve.platform.exception.*;
@Service @Transactional public class VisitorSignalService {
    private static final Set<String> EVENTS=Set.of("creator_view","form_view","submission_attempt","petal_click","sns_click","qr_visit");
    private final JdbcTemplate jdbc;
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final MessageRepository messages;
    private final Clock clock;
    public VisitorSignalService(JdbcTemplate jdbc,EventService events,CreatorService creators,ArtworkService artworks,MessageRepository messages,Clock clock) {
        this.jdbc=jdbc;
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.messages=messages;
        this.clock=clock;
    }
    private void scope(Long eventId,Long creatorId,Long artworkId) {
        events.requirePublished(eventId);
        if(creatorId!=null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(eventId,creatorId);
        }
        if(artworkId!=null) {
            var a=artworks.requirePublished(artworkId);
            artworks.validateExhibition(eventId,artworkId);
            if(creatorId!=null&&!creatorId.equals(a.getCreatorId()))throw new InvalidParticipationException();
        }
    }
    public void signal(String eventName,Long eventId,Long creatorId,Long artworkId) {
        if(!EVENTS.contains(eventName)||eventId==null)throw new InvalidMessageException();
        scope(eventId,creatorId,artworkId);
        jdbc.update("insert into analytics_events(event_name,event_id,creator_id,artwork_id,created_at) values(?,?,?,?,?)",eventName,eventId,creatorId,artworkId,LocalDateTime.now(clock));
    }
    public void report(Long id,String reason) {
        var m=messages.findById(id).filter(x->x.getStatus()==MessageStatus.PUBLISHED).orElseThrow(InvalidMessageException::new);
        scope(m.getEventId(),m.getCreatorId(),m.getArtworkId());
        if(reason==null||reason.isBlank()||reason.length()>300)throw new InvalidMessageException();
        jdbc.update("insert into message_reports(message_id,reason,created_at,updated_at) values(?,?,?,?)",id,reason.trim(),LocalDateTime.now(clock),LocalDateTime.now(clock));
    }
    public List<Map<String,Object>> reports(int page) {
        return jdbc.queryForList("select r.report_id,r.message_id,r.reason,r.status,r.created_at,m.message from message_reports r join messages m on m.message_id=r.message_id order by r.created_at desc limit 30 offset ?",Math.max(0,page)*30);
    }
    public void review(Long id) {
        jdbc.update("update message_reports set status='REVIEWED',updated_at=? where report_id=?",LocalDateTime.now(clock),id);
    }
    public Map<String,Object> summary(Long eventId) {
        return jdbc.queryForMap("select count(*) filter(where event_name='submission_attempt') as attempts,count(*) filter(where event_name='submission_complete') as completions,case when count(*) filter(where event_name='submission_attempt')=0 then null else round(100.0*count(*) filter(where event_name='submission_complete')/count(*) filter(where event_name='submission_attempt'),1) end as completion_rate from analytics_events where event_id=?",eventId);
    }
    public List<Map<String,Object>> creatorTotals(Long eventId) {
        return jdbc.queryForList("select c.creator_id,c.name,count(m.message_id) filter(where m.status<>'DELETED') as total,count(m.message_id) filter(where m.status='PUBLISHED') as published from event_creators ec join creators c on c.creator_id=ec.creator_id left join messages m on m.creator_id=c.creator_id and m.event_id=ec.event_id where ec.event_id=? group by c.creator_id,c.name order by c.creator_id",eventId);
    }
    public List<Map<String,Object>> analytics(Long eventId) {
        return jdbc.queryForList("select event_name,count(*) as total from analytics_events where event_id=? group by event_name order by event_name",eventId);
    }
}
