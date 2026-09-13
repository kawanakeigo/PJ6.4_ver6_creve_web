package world.creve.playpit.service;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.service.EventService;
import world.creve.playpit.dto.response.*;
import world.creve.playpit.util.PetalLayout;
@Service @Transactional(readOnly=true) public class UkaService {
    private final MessageService messages;
    private final EventService events;
    public UkaService(MessageService messages,EventService events) {
        this.messages=messages;
        this.events=events;
    }
    public int calculateGrowthStage(long messageCount) {
        return PetalLayout.growth(messageCount);
    }
    public UkaResponse getUkaData(Long eventId) {
        return getUkaData(eventId,null,null);
    }
    public UkaResponse getUkaData(Long eventId,Long creatorId,Long artworkId) {
        var event=events.requirePublished(eventId);
        var page=messages.getPublishedMessages(eventId,creatorId,artworkId,0,300);
        return new UkaResponse(eventId,event.getTitle(),page.getTotalElements(),calculateGrowthStage(page.getTotalElements()),page.getContent().stream().map(this::toPetal).toList());
    }
    public PetalResponse toPetal(MessageRow m) {
        var p=PetalLayout.position(m.petalSeed());
        return new PetalResponse(m.messageId(),m.petalType(),p.x(),p.y(),p.rotation(),p.scale(),m.message(),m.publicName(),m.artworkTitle(),m.creatorName(),m.createdAt().toString());
    }
}
