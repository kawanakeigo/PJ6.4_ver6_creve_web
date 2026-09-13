package world.creve.playpit.dto.request;
import jakarta.validation.constraints.*;
import java.time.*;
import org.springframework.format.annotation.DateTimeFormat;
public class EventForm {
    @NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{0,19}") private String eventType;
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String value) {
        eventType=value;
    }
    @NotBlank @Size(max=100) private String slug;
    public String getSlug() {
        return slug;
    }
    public void setSlug(String value) {
        slug=value;
    }
    @NotBlank @Size(max=150) private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String value) {
        title=value;
    }
    @Size(max=300) private String summary;
    public String getSummary() {
        return summary;
    }
    public void setSummary(String value) {
        summary=value;
    }
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        description=value;
    }
    @Size(max=500) private String mainImageUrl;
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public void setMainImageUrl(String value) {
        mainImageUrl=value;
    }
    @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) private LocalDateTime startAt;
    public LocalDateTime getStartAt() {
        return startAt;
    }
    public void setStartAt(LocalDateTime value) {
        startAt=value;
    }
    @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) private LocalDateTime endAt;
    public LocalDateTime getEndAt() {
        return endAt;
    }
    public void setEndAt(LocalDateTime value) {
        endAt=value;
    }
    @Size(max=150) private String venueName;
    public String getVenueName() {
        return venueName;
    }
    public void setVenueName(String value) {
        venueName=value;
    }
    @Size(max=300) private String address;
    public String getAddress() {
        return address;
    }
    public void setAddress(String value) {
        address=value;
    }
    private String access;
    public String getAccess() {
        return access;
    }
    public void setAccess(String value) {
        access=value;
    }
    @Size(max=100) private String price;
    public String getPrice() {
        return price;
    }
    public void setPrice(String value) {
        price=value;
    }
    @Size(max=500) private String ticketUrl;
    public String getTicketUrl() {
        return ticketUrl;
    }
    public void setTicketUrl(String value) {
        ticketUrl=value;
    }
    @NotBlank @Pattern(regexp="DRAFT|PUBLISHED|HIDDEN|ARCHIVED") private String status;
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        status=value;
    }
}
