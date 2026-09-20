package world.creve.playpit.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.exception.InvalidMessageException;
import world.creve.platform.exception.InvalidParticipationException;
import world.creve.platform.exception.RateLimitExceededException;
import world.creve.platform.security.SessionIdentity;
import world.creve.platform.service.ArtworkService;
import world.creve.platform.service.CreatorService;
import world.creve.platform.service.EventService;
import world.creve.playpit.entity.MessageStatus;
import world.creve.playpit.repository.MessageRepository;

@Service
@Transactional
public class VisitorSignalService {
    private static final Set<String> KINDS = Set.of("EVENT_VIEW", "CREATOR_VIEW", "ARTWORK_VIEW");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    public record ReportReceipt(long reportId, boolean created) {}
    private final JdbcTemplate jdbc;
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final MessageRepository messages;
    private final SessionIdentity identity;
    private final Clock clock;

    public VisitorSignalService(
            JdbcTemplate jdbc,
            EventService events,
            CreatorService creators,
            ArtworkService artworks,
            MessageRepository messages,
            SessionIdentity identity,
            Clock clock) {
        this.jdbc = jdbc;
        this.events = events;
        this.creators = creators;
        this.artworks = artworks;
        this.messages = messages;
        this.identity = identity;
        this.clock = clock;
    }

    private void scope(Long eventId, Long creatorId, Long artworkId) {
        events.requirePublished(eventId);
        if (creatorId != null) {
            creators.requirePublished(creatorId);
            creators.validateCreatorParticipation(eventId, creatorId);
        }
        if (artworkId != null) {
            var artwork = artworks.requirePublished(artworkId);
            artworks.validateExhibition(eventId, artworkId);
            if (creatorId != null && !creatorId.equals(artwork.getCreatorId())) {
                throw new InvalidParticipationException();
            }
        }
    }

    public void signal(UUID metricId, String kind, Long eventId, Long creatorId, Long artworkId) {
        String normalized = kind == null ? "" : kind.toUpperCase(java.util.Locale.ROOT);
        if (!KINDS.contains(normalized) || eventId == null) throw new InvalidMessageException();
        scope(eventId, creatorId, artworkId);
        jdbc.update("insert into pp_metrics(metric_id,kind,event_id,creator_id,artwork_id,visitor_hash,occurred_at) " +
                        "values(?,?,?,?,?,?,?) on conflict(metric_id) do nothing",
                metricId, normalized, eventId, creatorId, artworkId, identity.hash(),
                java.time.OffsetDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC));
    }

    public ReportReceipt report(Long messageId, String category, String detail) {
        var message = messages.findById(messageId)
                .filter(item -> item.getStatus() == MessageStatus.PUBLISHED)
                .orElseThrow(InvalidMessageException::new);
        scope(message.getEventId(), message.getCreatorId(), message.getArtworkId());
        String normalizedCategory = category == null ? "OTHER" : category.toUpperCase(java.util.Locale.ROOT);
        String normalizedDetail = detail == null ? "" : detail.strip();
        if (!Set.of("RIGHTS", "ABUSE", "OTHER").contains(normalizedCategory)
                || normalizedDetail.isEmpty()
                || normalizedDetail.codePointCount(0, normalizedDetail.length()) > 1000) {
            throw new InvalidMessageException();
        }
        Instant instant = clock.instant();
        LocalDateTime now = LocalDateTime.ofInstant(instant, TOKYO);
        String subjectHash = identity.hash();
        OffsetDateTime lockTime = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        jdbc.update("insert into pp_report_subject_locks(subject_hash,last_used_at) values(?,?) " +
                "on conflict(subject_hash) do nothing", subjectHash, lockTime);
        jdbc.queryForObject("select subject_hash from pp_report_subject_locks where subject_hash=? for update",
                String.class, subjectHash);
        jdbc.update("update pp_report_subject_locks set last_used_at=? where subject_hash=?", lockTime, subjectHash);

        var duplicate = jdbc.queryForList(
                "select report_id from pp_reports where subject_hash=? and message_id=? and created_at>? " +
                        "order by created_at desc,report_id desc limit 1",
                subjectHash, messageId, now.minusMinutes(10));
        if (!duplicate.isEmpty()) {
            return new ReportReceipt(((Number) duplicate.getFirst().get("report_id")).longValue(), false);
        }
        Long accepted = jdbc.queryForObject(
                "select count(*) from pp_reports where subject_hash=? and created_at>?",
                Long.class, subjectHash, now.minusMinutes(10));
        if (accepted != null && accepted >= 5) {
            throw new RateLimitExceededException("通報回数の上限に達しました。時間をおいてください。");
        }
        Long reportId = jdbc.queryForObject(
                "insert into pp_reports(message_id,category,detail,state,subject_hash,created_at,updated_at) " +
                        "values(?,?,?,'OPEN',?,?,?) returning report_id",
                Long.class, messageId, normalizedCategory, normalizedDetail, subjectHash, now, now);
        return new ReportReceipt(reportId, true);
    }

    public List<Map<String, Object>> reports(int page) {
        return jdbc.queryForList("select r.report_id,r.message_id,r.category,r.detail,r.state,r.created_at,m.message " +
                        "from pp_reports r join messages m on m.message_id=r.message_id " +
                        "order by r.created_at desc,r.report_id desc limit 20 offset ?",
                Math.max(0, page) * 20);
    }

    public void review(Long id) {
        jdbc.update("update pp_reports set state='IN_REVIEW',version=version+1,updated_at=? " +
                        "where report_id=? and state='OPEN'",
                java.time.LocalDateTime.ofInstant(clock.instant(), java.time.ZoneId.of("Asia/Tokyo")), id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary(Long eventId) {
        return jdbc.queryForMap("select count(*) filter(where kind='EVENT_VIEW') as event_views," +
                "count(*) filter(where kind='CREATOR_VIEW') as creator_views," +
                "count(*) filter(where kind='ARTWORK_VIEW') as artwork_views," +
                "count(*) filter(where kind='MESSAGE_ACCEPTED') as messages " +
                "from pp_metrics where event_id=?", eventId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> creatorTotals(Long eventId) {
        return jdbc.queryForList("select c.creator_id,c.name," +
                "count(m.message_id) filter(where m.status<>'DELETED') as total," +
                "count(m.message_id) filter(where m.status='PUBLISHED') as published " +
                "from event_creators ec join creators c on c.creator_id=ec.creator_id " +
                "left join messages m on m.creator_id=c.creator_id and m.event_id=ec.event_id " +
                "where ec.event_id=? group by c.creator_id,c.name order by c.creator_id", eventId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> analytics(Long eventId) {
        return jdbc.queryForList("select kind,count(*) as total from pp_metrics " +
                "where event_id=? group by kind order by kind", eventId);
    }
}
