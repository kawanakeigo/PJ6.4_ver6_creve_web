package world.creve.playpit.repository;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.exception.InvalidMessageException;
@Repository public class SubmissionStore {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    public SubmissionStore(JdbcTemplate jdbc,Clock clock) {
        this.jdbc=jdbc;
        this.clock=clock;
    }
    /** Both insert and row lock execute inside the caller's transaction. Across app instances too. */
    public void lock(String hash,LocalDateTime now) {
        jdbc.update("insert into posting_sessions(session_hash,last_seen_at) values(?,?) on conflict(session_hash) do nothing",hash,now);
        jdbc.queryForObject("select session_hash from posting_sessions where session_hash=? for update",String.class,hash);
        jdbc.update("update posting_sessions set last_seen_at=? where session_hash=?",now,hash);
        jdbc.update("delete from message_submission_receipts where session_hash=? and expires_at<=?",hash,now);
    }
    public Optional<Long> replay(String hash,String key,String requestHash) {
        if(key==null)return Optional.empty();
        var rows=jdbc.queryForList("select message_id,request_hash from message_submission_receipts where session_hash=? and idempotency_key=? and expires_at>?",hash,key,LocalDateTime.now(clock));
        if(rows.isEmpty())return Optional.empty();
        var row=rows.get(0);
        if(!requestHash.equals(row.get("request_hash")))throw new InvalidMessageException("再送内容が一致しません。");
        return Optional.of(((Number)row.get("message_id")).longValue());
    }
    public void record(String hash,String key,String requestHash,String bodyHash,Long messageId,LocalDateTime now) {
        jdbc.update("insert into message_submission_receipts(session_hash,idempotency_key,request_hash,body_hash,message_id,created_at,expires_at) values(?,?,?,?,?,?,?)",hash,key,requestHash,bodyHash,messageId,now,now.plusMinutes(10));
    }
    public void postingAnalytics(Long eventId,Long creatorId,Long artworkId,LocalDateTime now) {
        jdbc.update("insert into analytics_events(event_name,event_id,creator_id,artwork_id,created_at) values('submission_complete',?,?,?,?)",eventId,creatorId,artworkId,now);
    }
    public long nextPetalOrdinal(Long eventId) {
        return jdbc.queryForObject("insert into event_petal_sequences(event_id,next_value) values(?,1) on conflict(event_id) do update set next_value=event_petal_sequences.next_value+1 returning next_value",Long.class,eventId);
    }
    @Transactional @Scheduled(fixedDelay=600000) public void cleanup() {
        LocalDateTime now=LocalDateTime.now(clock);
        jdbc.update("delete from message_submission_receipts where expires_at<?",now);
        jdbc.update("delete from posting_sessions p where last_seen_at<? and not exists(select 1 from message_submission_receipts r where r.session_hash=p.session_hash)",now.minusMinutes(20));
    }
}
