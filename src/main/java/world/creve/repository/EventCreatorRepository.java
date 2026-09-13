package world.creve.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.EventCreator;

public interface EventCreatorRepository extends JpaRepository<EventCreator, Long> {
  List<EventCreator> findByEventIdOrderByDisplayOrderAsc(String eventId);

  boolean existsByEventIdAndCreatorId(String eventId, String creatorId);
}
