package world.creve.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.creve.entity.Creator;
import world.creve.entity.PublishStatus;

public interface CreatorRepository extends JpaRepository<Creator, String> {
  Optional<Creator> findBySlugAndStatus(String slug, PublishStatus status);

  List<Creator> findByStatusOrderByDisplayOrderAsc(PublishStatus status);

  @Query("""
      select creator from Creator creator
      join EventCreator eventCreator on eventCreator.creatorId = creator.id
      where eventCreator.eventId = :eventId
        and creator.status = :status
      order by eventCreator.displayOrder asc, creator.displayOrder asc
      """)
  List<Creator> findCreatorsByEventId(
      @Param("eventId") String eventId,
      @Param("status") PublishStatus status
  );
}
