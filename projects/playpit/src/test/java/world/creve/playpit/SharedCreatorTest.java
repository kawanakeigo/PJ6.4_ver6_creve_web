package world.creve.playpit;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.data.domain.Page;
import world.creve.platform.dto.*;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.service.*;
import world.creve.platform.exception.InvalidParticipationException;
import world.creve.platform.exception.InvalidMessageException;
import world.creve.playpit.controller.CreatorController;
import world.creve.playpit.service.*;
import world.creve.playpit.dto.request.CreatorForm;
import world.creve.playpit.dto.response.UkaResponse;
class SharedCreatorTest {
    final EventService events=mock(EventService.class);
    final CreatorService creators=mock(CreatorService.class);
    final ArtworkService artworks=mock(ArtworkService.class);
    final MessageService messages=mock(MessageService.class);
    
    final CreatorController controller=new CreatorController(events,creators,artworks,messages);
    EventResponse event(long id,String slug) { return new EventResponse(id,"PLAYPIT",slug,slug,null,null,null,null,null,null); }
    void profile() { when(creators.getCreatorDetailBySlug("artist")).thenReturn(new CreatorDetailResponse(7L,"artist","作者",null,"共通プロフィール",null,null,null,null,List.of())); }
    @Test void twoEventsUseSameProfileButSeparateWorksAndMessages() {
        profile();
        when(events.getPublishedEventsByCreatorId(7L)).thenReturn(List.of(event(1,"first"),event(2,"second")));
        for(long id:List.of(1L,2L)) {
            when(messages.getPublishedMessages(id,7L,null,0,30)).thenReturn(Page.empty());
            var model=new ExtendedModelMap();
            assertEquals("playpit/global-creator-detail",controller.showGlobalCreatorDetail("artist",id==1?"first":"second",0,0,model));
            assertEquals(7L,model.getAttribute("creatorId"));
            verify(artworks).getArtworksByEventAndCreator(id,7L,0);
            verify(messages).getPublishedMessages(id,7L,null,0,30);
        }
        verify(artworks,never()).getPublishedArtworksByCreatorId(anyLong(),anyInt());
        assertThrows(InvalidParticipationException.class,()->controller.showGlobalCreatorDetail("artist","unrelated",0,0,new ExtendedModelMap()));
    }
    @Test void legacyLinkPreservesContextAndValidatesParticipation() {
        profile(); var detail=mock(EventDetailResponse.class); when(detail.eventId()).thenReturn(1L); when(detail.slug()).thenReturn("first"); when(events.getPublishedPlaypitEventBySlug("first")).thenReturn(detail);
        var response=controller.showCreatorDetail("first","artist",2,3,"qr");
        assertEquals(308,response.getStatusCode().value());
        assertEquals("/creators/artist?event=first&page=2&artworkPage=3&source=qr",response.getHeaders().getLocation().toString());
        verify(creators).validateCreatorParticipation(1L,7L);
        doThrow(new InvalidParticipationException()).when(creators).validateCreatorParticipation(1L,7L);
        assertThrows(InvalidParticipationException.class,()->controller.showCreatorDetail("first","artist",0,0,null));
    }
    @Test void creatorWithoutPublishedParticipationKeepsPortfolio() {
        profile(); when(events.getPublishedEventsByCreatorId(7L)).thenReturn(List.of());
        controller.showGlobalCreatorDetail("artist",null,0,0,new ExtendedModelMap());
        verify(artworks).getPublishedArtworksByCreatorId(7L,0);
        verifyNoInteractions(messages);
    }
    @Test void adminReusesCreatorAndExistingParticipationWithoutCopyingProfile() {
        var er=mock(EventRepository.class); var cr=mock(CreatorRepository.class); var pr=mock(EventCreatorRepository.class);
        var service=new ContentAdminService(er,cr,mock(ArtworkRepository.class),pr,mock(EventArtworkRepository.class),mock(CreatorLinkRepository.class),mock(MediaFileRepository.class));
        var creator=new Creator();creator.setCreatorId(7L);creator.setSlug("artist");creator.setProfile("保持");
        when(cr.findById(7L)).thenReturn(Optional.of(creator));
        when(er.findById(anyLong())).thenReturn(Optional.of(new Event()));
        var first=new EventCreator();first.setEventId(1L);first.setCreatorId(7L);
        when(pr.findByEventIdAndCreatorId(1L,7L)).thenReturn(Optional.of(first));
        service.addParticipant(1L,7L,3,"初回"); service.addParticipant(2L,7L,1,"次回");
        verify(pr).save(first);
        verify(pr).save(argThat(p->p.getEventId().equals(2L)&&p.getCreatorId().equals(7L)&&"次回".equals(p.getExhibitionTitle())));
        verify(cr,never()).save(any());verify(cr,never()).saveAndFlush(any());
        assertEquals("保持",creator.getProfile());
        var form=new CreatorForm();form.setSlug("changed");
        assertThrows(InvalidMessageException.class,()->service.saveCreator(7L,form));
        assertEquals("artist",creator.getSlug());
    }
}
