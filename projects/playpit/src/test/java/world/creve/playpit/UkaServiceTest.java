package world.creve.playpit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.*;
import java.util.*;
import java.time.*;
import world.creve.playpit.service.*;
import world.creve.playpit.dto.response.*;
import world.creve.playpit.util.PetalLayout;
import world.creve.platform.entity.Event;
import world.creve.platform.service.EventService;
class UkaServiceTest {
    @Test void realDisplayServiceAsksFor300AndKeepsTotal() {
        var messages=mock(MessageService.class);
        var events=mock(EventService.class);
        Event e=new Event();
        e.setTitle("展示");
        when(events.requirePublished(1L)).thenReturn(e);
        when(messages.getPublishedMessages(1L,null,null,0,300)).thenReturn(new PageImpl<MessageRow>(List.of(),PageRequest.of(0,300),400));
        var r=new UkaService(messages,events).getUkaData(1L);
        assertEquals(400L,r.messageCount());
        assertEquals(4,r.growthStage());
        verify(messages).getPublishedMessages(1L,null,null,0,300);
    }
    @Test void anonymousPublicDtoNeverLeaksInputName() {
        var s=new UkaService(mock(MessageService.class),mock(EventService.class));
        var m=new MessageRow(1L,2L,3L,"感想","公開しない名前",true,1,PetalLayout.seed(1,1),LocalDateTime.of(2026,9,14,9,0),"作品","作者");
        var p=s.toPetal(m);
        assertEquals("匿名",p.displayName());
        assertEquals("作品",p.artworkTitle());
        assertEquals("作者",p.creatorName());
        assertEquals("2026-09-14T09:00",p.createdAt());
    }
}
