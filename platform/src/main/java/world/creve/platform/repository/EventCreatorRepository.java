package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface EventCreatorRepository extends JpaRepository<EventCreator,Long> {
    @Query("select new world.creve.platform.dto.RankingParticipation(ec.creatorId,e.eventId,e.startAt,ec.spaceType) from EventCreator ec join Event e on e.eventId=ec.eventId where e.eventType='PLAYPIT' and e.status='PUBLISHED'")
    List<RankingParticipation> findPublicRankingParticipations();
    boolean existsByEventIdAndCreatorId(Long eventId,Long creatorId);
    Optional<EventCreator> findByEventIdAndCreatorId(Long eventId,Long creatorId);
    Page<EventCreator> findByEventIdOrderByDisplayOrderAsc(Long eventId,Pageable pageable);
}
