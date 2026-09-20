package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface EventArtworkRepository extends JpaRepository<EventArtwork,Long> {
    boolean existsByEventIdAndArtworkId(Long eventId,Long artworkId);
    Optional<EventArtwork> findByEventIdAndArtworkId(Long eventId,Long artworkId);
    Page<EventArtwork> findByEventIdOrderByDisplayOrderAsc(Long eventId,Pageable pageable);
}
