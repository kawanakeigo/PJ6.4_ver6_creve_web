package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="events") public class Event {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="event_id") private Long eventId;
    @Column(name="event_type", nullable=false, length=20) private String eventType;
    @Column(name="slug", nullable=false, length=100, unique=true) private String slug;
    @Column(name="title", nullable=false, length=150) private String title;
    @Column(name="summary", nullable=true, length=300) private String summary;
    @Column(name="description", nullable=true, columnDefinition="TEXT") private String description;
    @Column(name="main_image_url", nullable=true, length=500) private String mainImageUrl;
    @Column(name="start_at", nullable=false) private LocalDateTime startAt;
    @Column(name="end_at", nullable=false) private LocalDateTime endAt;
    @Column(name="venue_name", nullable=true, length=150) private String venueName;
    @Column(name="address", nullable=true, length=300) private String address;
    @Column(name="access", nullable=true, columnDefinition="TEXT") private String access;
    @Column(name="price", nullable=true, length=100) private String price;
    @Column(name="ticket_url", nullable=true, length=500) private String ticketUrl;
    @Column(name="status", nullable=false, length=20) private String status;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public Event() {
    }
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long value) {
        this.eventId=value;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String value) {
        this.eventType=value;
    }
    public String getSlug() {
        return slug;
    }
    public void setSlug(String value) {
        this.slug=value;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String value) {
        this.title=value;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String value) {
        this.summary=value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description=value;
    }
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public void setMainImageUrl(String value) {
        this.mainImageUrl=value;
    }
    public LocalDateTime getStartAt() {
        return startAt;
    }
    public void setStartAt(LocalDateTime value) {
        this.startAt=value;
    }
    public LocalDateTime getEndAt() {
        return endAt;
    }
    public void setEndAt(LocalDateTime value) {
        this.endAt=value;
    }
    public String getVenueName() {
        return venueName;
    }
    public void setVenueName(String value) {
        this.venueName=value;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String value) {
        this.address=value;
    }
    public String getAccess() {
        return access;
    }
    public void setAccess(String value) {
        this.access=value;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String value) {
        this.price=value;
    }
    public String getTicketUrl() {
        return ticketUrl;
    }
    public void setTicketUrl(String value) {
        this.ticketUrl=value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status=value;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime value) {
        this.createdAt=value;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime value) {
        this.updatedAt=value;
    }
    @PrePersist protected void insertTimestamp() {
        LocalDateTime now=LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        if(createdAt==null)createdAt=now;
        if(updatedAt==null)updatedAt=now;
    }
    @PreUpdate protected void updateTimestamp() {
        updatedAt=LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }
}
