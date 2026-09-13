package world.creve.service;

import java.util.List;
import org.springframework.stereotype.Service;
import world.creve.entity.Artwork;
import world.creve.entity.PublishStatus;
import world.creve.exception.NotFoundException;
import world.creve.repository.ArtworkRepository;
import world.creve.repository.EventArtworkRepository;

@Service
public class ArtworkService {
  private final ArtworkRepository artworkRepository;
  private final EventArtworkRepository eventArtworkRepository;

  public ArtworkService(
      ArtworkRepository artworkRepository,
      EventArtworkRepository eventArtworkRepository
  ) {
    this.artworkRepository = artworkRepository;
    this.eventArtworkRepository = eventArtworkRepository;
  }

  public List<Artwork> findAllForAdmin() {
    return artworkRepository.findAll();
  }

  public List<Artwork> findByEventId(String eventId) {
    return getArtworksByEventId(eventId);
  }

  public List<Artwork> findByCreator(String eventId, String creatorId) {
    return getArtworksByEventIdAndCreatorId(eventId, creatorId);
  }

  public List<Artwork> getArtworksByEventId(String eventId) {
    return artworkRepository.findArtworksByEventId(eventId, PublishStatus.PUBLISHED);
  }

  public List<Artwork> getArtworksByEventIdAndCreatorId(String eventId, String creatorId) {
    return artworkRepository.findArtworksByEventIdAndCreatorId(
        eventId,
        creatorId,
        PublishStatus.PUBLISHED
    );
  }

  public Artwork findById(String artworkId) {
    return artworkRepository.findById(artworkId)
        .orElseThrow(() -> new NotFoundException("作品が見つかりません。"));
  }

  public Artwork findBySlug(String artworkSlug) {
    return artworkRepository.findBySlugAndStatus(artworkSlug, PublishStatus.PUBLISHED)
        .or(() -> artworkRepository.findById(artworkSlug)
            .filter(artwork -> artwork.getStatus() == PublishStatus.PUBLISHED))
        .orElseThrow(() -> new NotFoundException("作品が見つかりません。"));
  }

  public Artwork getArtworkForEvent(String eventId, String artworkSlug) {
    Artwork artwork = findBySlug(artworkSlug);
    if (!eventArtworkRepository.existsByEventIdAndArtworkId(eventId, artwork.getId())) {
      throw new NotFoundException("イベント展示作品が見つかりません。");
    }
    return artwork;
  }

  public boolean isJoinedEvent(String eventId, String artworkId) {
    return eventArtworkRepository.existsByEventIdAndArtworkId(eventId, artworkId);
  }
}
