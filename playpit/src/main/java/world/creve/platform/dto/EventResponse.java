package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record EventResponse(Long eventId, String eventType, String slug, String title, String summary, String mainImageUrl, LocalDateTime startAt, LocalDateTime endAt, String venueName, String phase) {
    public Long getEventId() {
        return eventId;
    }
    public String getEventType() {
        return eventType;
    }
    public String getSlug() {
        return slug;
    }
    public String getTitle() {
        return title;
    }
    public String getSummary() {
        return summary;
    }
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public LocalDateTime getStartAt() {
        return startAt;
    }
    public LocalDateTime getEndAt() {
        return endAt;
    }
    public String getVenueName() {
        return venueName;
    }
    public String getPhase() {
        return phase;
    }
}
