package world.creve.playpit.repository;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.exception.IdempotencyConflictException;

@Repository
public class SubmissionStore {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public SubmissionStore(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public void lock(String subjectHash, Instant now) {
        jdbc.update("insert into pp_subject_locks(subject_hash,last_used_at) values(?,?) " +
                "on conflict(subject_hash) do nothing", subjectHash, db(now));
        jdbc.queryForObject("select subject_hash from pp_subject_locks where subject_hash=? for update",
                String.class, subjectHash);
        jdbc.update("update pp_subject_locks set last_used_at=? where subject_hash=?", db(now), subjectHash);
        jdbc.update("delete from pp_post_receipts where subject_hash=? and expires_at<=?", subjectHash, db(now));
    }

    public Optional<Long> replay(String subjectHash, UUID key, String requestHash, Instant now) {
        var rows = jdbc.queryForList(
                "select message_id,request_hash from pp_post_receipts " +
                        "where subject_hash=? and request_key=? and expires_at>?",
                subjectHash, key, db(now));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        var row = rows.getFirst();
        if (!requestHash.equals(row.get("request_hash"))) {
            throw new IdempotencyConflictException();
        }
        return Optional.of(((Number) row.get("message_id")).longValue());
    }

    public Optional<Long> findReceipt(String subjectHash, UUID key, Instant now) {
        var rows = jdbc.queryForList(
                "select message_id from pp_post_receipts where subject_hash=? and request_key=? and expires_at>?",
                subjectHash, key, db(now));
        return rows.isEmpty()
                ? Optional.empty()
                : Optional.of(((Number) rows.getFirst().get("message_id")).longValue());
    }

    public void enforceLimits(String subjectHash, String bodyHash, Instant now) {
        Long recent = jdbc.queryForObject(
                "select count(*) from pp_post_receipts where subject_hash=? and accepted_at>?",
                Long.class, subjectHash, db(now.minusSeconds(60)));
        Boolean duplicate = jdbc.queryForObject(
                "select exists(select 1 from pp_post_receipts where subject_hash=? " +
                        "and normalized_body_hash=? and accepted_at>?)",
                Boolean.class, subjectHash, bodyHash, db(now.minusSeconds(600)));
        if (recent != null && recent >= 3) {
            throw new world.creve.platform.exception.RateLimitExceededException();
        }
        if (Boolean.TRUE.equals(duplicate)) {
            throw new world.creve.platform.exception.RateLimitExceededException("同じ感想は10分後に投稿できます。");
        }
    }

    public void record(
            String subjectHash,
            UUID key,
            String requestHash,
            String bodyHash,
            Long messageId,
            String responseJson,
            Instant now) {
        jdbc.update("insert into pp_post_receipts(subject_hash,request_key,request_hash," +
                        "normalized_body_hash,message_id,accepted_at,response_json,expires_at) " +
                        "values(?,?,?,?,?,?,cast(? as jsonb),?)",
                subjectHash, key, requestHash, bodyHash, messageId, db(now), responseJson, db(now.plusSeconds(86_400)));
    }

    public void messageAcceptedMetric(UUID metricId, Long eventId, Long creatorId, Long artworkId, String visitorHash, Instant now) {
        jdbc.update("insert into pp_metrics(metric_id,kind,event_id,creator_id,artwork_id,visitor_hash,occurred_at) " +
                        "values(?,'MESSAGE_ACCEPTED',?,?,?,?,?) on conflict(metric_id) do nothing",
                metricId, eventId, creatorId, artworkId, visitorHash, db(now));
    }

    @Transactional
    @Scheduled(fixedDelay = 600_000)
    public void cleanup() {
        Instant now = clock.instant();
        jdbc.update("delete from pp_post_receipts where expires_at<?", db(now));
        jdbc.update("delete from pp_subject_locks s where last_used_at<? and not exists(" +
                "select 1 from pp_post_receipts r where r.subject_hash=s.subject_hash)", db(now.minusSeconds(172_800)));
    }

    private OffsetDateTime db(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
