package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface EventCreatorRepository extends JpaRepository<EventCreator,Long> {
    boolean existsByEventIdAndCreatorId(Long eventId,Long creatorId);
    Optional<EventCreator> findByEventIdAndCreatorId(Long eventId,Long creatorId);
    Page<EventCreator> findByEventIdOrderByDisplayOrderAsc(Long eventId,Pageable pageable);
}
