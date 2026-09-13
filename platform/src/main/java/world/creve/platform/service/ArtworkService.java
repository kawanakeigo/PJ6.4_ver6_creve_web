package world.creve.platform.service;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.dto.*;
import world.creve.platform.exception.*;
import world.creve.platform.util.PageBounds;
@Service @Transactional(readOnly=true) public class ArtworkService {
    private final ArtworkRepository artworks;
    private final EventArtworkRepository exhibitions;
    private final CreatorService creators;
    private final MediaFileRepository media;
    public ArtworkService(ArtworkRepository artworks,EventArtworkRepository exhibitions,CreatorService creators,MediaFileRepository media) {
        this.artworks=artworks;
        this.exhibitions=exhibitions;
        this.creators=creators;
        this.media=media;
    }
    public Artwork requirePublished(Long id) {
        Artwork a=artworks.findById(id).filter(x->"PUBLISHED".equals(x.getStatus())).orElseThrow(ArtworkNotFoundException::new);
        creators.requirePublished(a.getCreatorId());
        return a;
    }
    public ArtworkDetailResponse getArtworkDetailBySlug(String slug) {
        return detail(artworks.findBySlugAndStatus(slug,"PUBLISHED").orElseThrow(ArtworkNotFoundException::new));
    }
    public void validateExhibition(Long eventId,Long artworkId) {
        if(!exhibitions.existsByEventIdAndArtworkId(eventId,artworkId))throw new InvalidParticipationException();
    }
    public List<ArtworkSummaryResponse> getArtworksByEventId(Long eventId) {
        return getArtworksByEventId(eventId,0);
    }
    public List<ArtworkSummaryResponse> getArtworksByEventId(Long eventId,int page) {
        return artworks.findArtworksByEventId(eventId,PageRequest.of(PageBounds.page(page),30));
    }
    public List<ArtworkSummaryResponse> getArtworksByEventAndCreator(Long eventId,Long creatorId) {
        return getArtworksByEventAndCreator(eventId,creatorId,0);
    }
    public List<ArtworkSummaryResponse> getArtworksByEventAndCreator(Long eventId,Long creatorId,int page) {
        return artworks.findArtworksByEventIdAndCreatorId(eventId,creatorId,PageRequest.of(PageBounds.page(page),30));
    }
    public ArtworkDetailResponse detail(Artwork a) {
        Creator c=creators.requirePublished(a.getCreatorId());
        return new ArtworkDetailResponse(a.getArtworkId(),a.getSlug(),a.getTitle(),a.getDescription(),a.getBackground(),a.getConcept(),a.getMaterials(),a.getProductionYear(),a.getMainImageUrl(),c.getCreatorId(),c.getSlug(),c.getName(),media.findByArtworkIdOrderByDisplayOrderAsc(a.getArtworkId()).stream().map(m->new MediaResponse(m.getMediaType(),m.getUrl(),m.getAltText())).toList());
    }
}
