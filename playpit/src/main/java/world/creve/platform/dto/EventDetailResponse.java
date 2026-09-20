package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record EventDetailResponse(Long eventId, String eventType, String slug, String title, String summary, String description, String mainImageUrl, LocalDateTime startAt, LocalDateTime endAt, String venueName, String address, String access, String price, String ticketUrl, String phase) {
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
    public String getDescription() {
        return description;
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
    public String getAddress() {
        return address;
    }
    public String getAccess() {
        return access;
    }
    public String getPrice() {
        return price;
    }
    public String getTicketUrl() {
        return ticketUrl;
    }
    public String getPhase() {
        return phase;
    }
}
