package world.creve.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.EventArtwork;

public interface EventArtworkRepository extends JpaRepository<EventArtwork, Long> {
  boolean existsByEventIdAndArtworkId(String eventId, String artworkId);

  List<EventArtwork> findByEventIdOrderByDisplayOrderAsc(String eventId);
}
