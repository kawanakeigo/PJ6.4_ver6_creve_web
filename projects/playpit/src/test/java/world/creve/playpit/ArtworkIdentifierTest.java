package world.creve.playpit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.exception.InvalidMessageException;
import world.creve.playpit.dto.request.ArtworkForm;
import world.creve.playpit.service.ContentAdminService;
class ArtworkIdentifierTest {
 final ArtworkRepository artworks=mock(ArtworkRepository.class);
 final CreatorRepository creators=mock(CreatorRepository.class);
 final MediaFileRepository media=mock(MediaFileRepository.class);
 final ContentAdminService service=new ContentAdminService(mock(EventRepository.class),creators,artworks,mock(EventCreatorRepository.class),mock(EventArtworkRepository.class),mock(CreatorLinkRepository.class),media);
 ArtworkForm form(){var f=new ArtworkForm();f.setCreatorId(1L);f.setTitle("日本語作品");f.setStatus("DRAFT");when(creators.findById(1L)).thenReturn(Optional.of(new Creator()));when(artworks.saveAndFlush(any())).thenAnswer(i->{Artwork a=i.getArgument(0);a.setArtworkId(7L);return a;});return f;}
 @Test void collisionGetsAnotherCandidate(){var f=form();when(artworks.existsBySlug(anyString())).thenReturn(true,false);service.saveArtwork(null,f);verify(artworks,times(2)).existsBySlug(anyString());verify(artworks).saveAndFlush(argThat(a->a.getSlug().matches("work-[a-f0-9]{32}")));}
 @Test void exhaustedCandidatesDoNotSave(){var f=form();when(artworks.existsBySlug(anyString())).thenReturn(true);assertThrows(InvalidMessageException.class,()->service.saveArtwork(null,f));verify(artworks,never()).saveAndFlush(any());}
 @Test void editKeepsLegacyUrlRegardlessOfSubmittedSlug(){var f=form();f.setSlug("別の識別子");var a=new Artwork();a.setArtworkId(7L);a.setCreatorId(1L);a.setSlug("legacy-url");when(artworks.findById(7L)).thenReturn(Optional.of(a));service.saveArtwork(7L,f);assertEquals("legacy-url",a.getSlug());verify(artworks,never()).existsBySlug(anyString());}
}
