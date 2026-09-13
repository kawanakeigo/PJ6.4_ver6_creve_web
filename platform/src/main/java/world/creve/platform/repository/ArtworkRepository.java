package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface ArtworkRepository extends JpaRepository<Artwork,Long> {
    Optional<Artwork> findBySlugAndStatus(String slug,String status);
    @Query("select new world.creve.platform.dto.ArtworkSummaryResponse(a.artworkId,a.slug,a.title,a.mainImageUrl,c.creatorId,c.slug,c.name) from Artwork a join Creator c on a.creatorId=c.creatorId join EventArtwork ea on ea.artworkId=a.artworkId where ea.eventId=:eventId and a.status='PUBLISHED' and c.status='PUBLISHED' order by ea.displayOrder,a.artworkId") List<ArtworkSummaryResponse> findArtworksByEventId(@Param("eventId") Long eventId,Pageable pageable);
    @Query("select new world.creve.platform.dto.ArtworkSummaryResponse(a.artworkId,a.slug,a.title,a.mainImageUrl,c.creatorId,c.slug,c.name) from Artwork a join Creator c on a.creatorId=c.creatorId join EventArtwork ea on ea.artworkId=a.artworkId where ea.eventId=:eventId and a.creatorId=:creatorId and a.status='PUBLISHED' and c.status='PUBLISHED' order by ea.displayOrder,a.artworkId") List<ArtworkSummaryResponse> findArtworksByEventIdAndCreatorId(@Param("eventId") Long eventId,@Param("creatorId") Long creatorId,Pageable pageable);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndArtworkIdNot(String slug,Long artworkId);
}
