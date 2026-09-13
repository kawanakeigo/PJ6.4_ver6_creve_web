package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record UkaResponse(Long eventId, String eventTitle, Long messageCount, Integer growthStage, List<PetalResponse> petals) {
    public Long getEventId() {
        return eventId;
    }
    public String getEventTitle() {
        return eventTitle;
    }
    public Long getMessageCount() {
        return messageCount;
    }
    public Integer getGrowthStage() {
        return growthStage;
    }
    public List<PetalResponse> getPetals() {
        return petals;
    }
}
