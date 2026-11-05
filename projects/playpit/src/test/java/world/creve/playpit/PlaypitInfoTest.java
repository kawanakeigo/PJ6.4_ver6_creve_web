package world.creve.playpit;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.data.domain.Page;
import world.creve.platform.service.EventService;
import world.creve.playpit.service.PlaypitContactService;
import world.creve.playpit.controller.PlaypitInfoController;
class PlaypitInfoTest {
    @TempDir Path directory;
    @Test void onlyPlaypitEventsSupplyNews() {
        var events=mock(EventService.class);var contact=mock(PlaypitContactService.class);
        when(events.list("PLAYPIT",2)).thenReturn(Page.empty());
        assertEquals("playpit/news",new PlaypitInfoController(events,contact,mock(world.creve.playpit.service.PlaypitNewsService.class)).news(2,new ExtendedModelMap()));
        verify(events).list("PLAYPIT",2);verifyNoMoreInteractions(events);verifyNoInteractions(contact);
    }
    @Test void missingAndBlankDedicatedContactRemainUnconfigured() throws Exception {
        var file=directory.resolve("playpit-contact.txt");var service=new PlaypitContactService(file.toString());
        Files.writeString(directory.resolve("contact.txt"),"CreVe contact must not be reused");
        assertNull(service.content());Files.writeString(file,"  ");assertNull(service.content());
        Files.writeString(file,"PLAYPITの確認済み案内");assertEquals("PLAYPITの確認済み案内",service.content());
        Files.writeString(file,"x".repeat(1_000_001));assertNull(service.content());
    }
    @Test void formUrlAcceptsOnlyDedicatedGoogleFormDestinations(){
        assertNull(PlaypitContactService.validatedFormUrl("https://evil.invalid/forms/test"));
        assertNull(PlaypitContactService.validatedFormUrl("javascript:alert(1)"));
        assertNull(PlaypitContactService.validatedFormUrl("https://user@forms.gle/test"));
        assertEquals("https://forms.gle/confirmed",PlaypitContactService.validatedFormUrl("https://forms.gle/confirmed"));
        assertEquals("https://docs.google.com/forms/d/e/confirmed/viewform",PlaypitContactService.validatedFormUrl("https://docs.google.com/forms/d/e/confirmed/viewform"));
    }
    @Test void dedicatedAnnouncementsOnlyPublishExplicitValidEntries() throws Exception {
        var file=directory.resolve("news.properties");var service=new world.creve.playpit.service.PlaypitNewsService(file.toString());
        assertTrue(service.notices().isEmpty());
        Files.writeString(file,"news.1.status=PUBLISHED\nnews.1.title=活動案内\nnews.1.date=2026-11-02\nnews.1.body=本文\nnews.2.status=DRAFT\nnews.2.title=非公開\nnews.2.date=2026-11-03\nnews.3.status=PUBLISHED\nnews.3.title=日付不正\nnews.3.date=invalid\n");
        assertEquals(1,service.notices().size());assertEquals("活動案内",service.notices().getFirst().title());
    }
}
