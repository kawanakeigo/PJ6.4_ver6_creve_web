package world.creve.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import world.creve.entity.Event;
import world.creve.entity.EventType;
import world.creve.entity.PublishStatus;

public interface EventRepository extends JpaRepository<Event, String> {
  Optional<Event> findBySlugAndStatus(String slug, PublishStatus status);

  Optional<Event> findBySlugAndEventTypeAndStatus(
      String slug,
      EventType eventType,
      PublishStatus status
  );

  List<Event> findByEventTypeAndStatusOrderByStartAtDesc(EventType eventType, PublishStatus status);

  List<Event> findTop3ByEventTypeAndStatusOrderByStartAtAsc(EventType eventType, PublishStatus status);

  @Query("""
      select event from Event event
      where event.status = :status
        and event.startAt >= :now
      order by event.startAt asc
      """)
  List<Event> findUpcomingEvents(PublishStatus status, OffsetDateTime now);
}
