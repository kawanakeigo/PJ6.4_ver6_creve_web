package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface EventRepository extends JpaRepository<Event,Long> {
    Optional<Event> findBySlugAndStatus(String slug,String status);
    Page<Event> findByEventTypeAndStatusOrderByStartAtDesc(String eventType,String status,Pageable pageable);
    @Query("select e from Event e where e.eventType=:type and e.status='PUBLISHED' and e.endAt>=:now order by e.startAt asc") List<Event> findUpcomingEvents(@Param("type") String type,@Param("now") LocalDateTime now,Pageable pageable);
    @Query("select e from Event e where e.eventType=:type and e.status='PUBLISHED' and e.endAt<:now order by e.startAt desc") Page<Event> findPastEvents(@Param("type") String type,@Param("now") LocalDateTime now,Pageable pageable);
    boolean existsBySlugAndEventIdNot(String slug,Long eventId);
    boolean existsBySlug(String slug);
}
