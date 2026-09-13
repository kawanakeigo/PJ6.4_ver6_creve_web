package world.creve.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.creve.entity.Artwork;
import world.creve.entity.PublishStatus;

public interface ArtworkRepository extends JpaRepository<Artwork, String> {
  Optional<Artwork> findBySlugAndStatus(String slug, PublishStatus status);

  @Query("""
      select artwork from Artwork artwork
      join EventArtwork eventArtwork on eventArtwork.artworkId = artwork.id
      where eventArtwork.eventId = :eventId
        and artwork.status = :status
      order by eventArtwork.displayOrder asc, artwork.displayOrder asc
      """)
  List<Artwork> findArtworksByEventId(
      @Param("eventId") String eventId,
      @Param("status") PublishStatus status
  );

  @Query("""
      select artwork from Artwork artwork
      join EventArtwork eventArtwork on eventArtwork.artworkId = artwork.id
      where eventArtwork.eventId = :eventId
        and artwork.creatorId = :creatorId
        and artwork.status = :status
      order by eventArtwork.displayOrder asc, artwork.displayOrder asc
      """)
  List<Artwork> findArtworksByEventIdAndCreatorId(
      @Param("eventId") String eventId,
      @Param("creatorId") String creatorId,
      @Param("status") PublishStatus status
  );
}
