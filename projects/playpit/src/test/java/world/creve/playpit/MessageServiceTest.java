package world.creve.playpit;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import world.creve.playpit.dto.request.*;
import world.creve.playpit.entity.*;
import world.creve.playpit.service.*;
import world.creve.playpit.repository.*;
import world.creve.platform.entity.*;
import world.creve.platform.service.*;
import world.creve.platform.security.*;
import world.creve.platform.exception.*;
class MessageServiceTest {
    private MessageRepository repo;
    private EventService events;
    private CreatorService creators;
    private ArtworkService artworks;
    private SubmissionStore store;
    private SessionIdentity identity;
    private MessageService service;
    private MessageRequest request;
    @BeforeEach void setup() {
        repo=mock(MessageRepository.class);
        events=mock(EventService.class);
        creators=mock(CreatorService.class);
        artworks=mock(ArtworkService.class);
        store=mock(SubmissionStore.class);
        identity=mock(SessionIdentity.class);
        Clock clock=Clock.fixed(Instant.parse("2026-09-14T00:00:00Z"),ZoneId.of("Asia/Tokyo"));
        service=new MessageService(repo,events,creators,artworks,store,identity,new IdentityHasher("test-only-not-production-secret-1234567890"),clock,true);
        Event e=new Event();
        e.setEventId(1L);
        e.setStatus("PUBLISHED");
        when(events.requirePublished(1L)).thenReturn(e);
        when(identity.hash()).thenReturn("hash");
        when(identity.idempotencyKey()).thenReturn("key");
        when(store.replay(anyString(),anyString(),anyString())).thenReturn(Optional.empty());
        when(store.nextPetalOrdinal(1L)).thenReturn(1L);
        when(repo.saveAndFlush(any(Message.class))).thenAnswer(inv-> {
            Message m=inv.getArgument(0);m.setMessageId(10L);return m;
        }
        );
        request=new MessageRequest();
        request.setEventId(1L);
        request.setCreatorId(2L);
        request.setMessage(" 感動しました ");
        request.setAnonymous(true);
        request.setAgreement(true);
    }
    @Test void storesDesignFields() {
        var response=service.registerMessage(request);
        assertEquals(10L,response.messageId());
        assertEquals("PUBLISHED",response.status());
        assertEquals("あなたの言葉が、一枚の花びらになりました。",response.completionMessage());
        verify(repo).saveAndFlush(argThat(m->m.getMessage().equals("感動しました")&&m.getCreatorId().equals(2L)&&m.getAnonymous()));
        verify(creators).validateCreatorParticipation(1L,2L);
    }
    @Test void rate429Exception() {
        when(repo.countRecentMessagesBySessionHash(anyString(),any())).thenReturn(3L);
        assertThrows(RateLimitExceededException.class,()->service.registerMessage(request));
        verify(repo,never()).saveAndFlush(any());
    }
    @Test void pending() {
        request.setMessage("a@example.invalid");
        assertEquals("PENDING",service.registerMessage(request).status());
    }
    @Test void mismatchedAuthor() {
        Artwork a=new Artwork();
        a.setArtworkId(3L);
        a.setCreatorId(4L);
        request.setArtworkId(3L);
        when(artworks.requirePublished(3L)).thenReturn(a);
        assertThrows(InvalidParticipationException.class,()->service.registerMessage(request));
        verify(repo,never()).saveAndFlush(any());
    }
    @Test void nonexistentEvent() {
        when(events.requirePublished(1L)).thenThrow(new EventNotFoundException());
        assertThrows(EventNotFoundException.class,()->service.registerMessage(request));
        verify(repo,never()).saveAndFlush(any());
    }
    @Test void replayDoesNotInsertOrCountTwice() {
        Message saved=new Message();
        saved.setMessageId(20L);
        saved.setStatus(MessageStatus.PUBLISHED);
        saved.setPetalType(1);
        saved.setPetalSeed(9L);
        saved.setCreatedAt(LocalDateTime.of(2026,9,14,9,0));
        when(store.replay(anyString(),anyString(),anyString())).thenReturn(Optional.of(20L));
        when(repo.findById(20L)).thenReturn(Optional.of(saved));
        assertEquals(20L,service.registerMessage(request).messageId());
        verify(repo,never()).saveAndFlush(any());
        verify(store,never()).postingAnalytics(any(),any(),any(),any());
    }
}
