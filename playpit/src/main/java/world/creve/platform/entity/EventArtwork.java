package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="event_artworks") public class EventArtwork {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="event_artwork_id") private Long eventArtworkId;
    @Column(name="event_id", nullable=false) private Long eventId;
    @Column(name="artwork_id", nullable=false) private Long artworkId;
    @Column(name="exhibition_area", nullable=true, length=150) private String exhibitionArea;
    @Column(name="display_order", nullable=false) private Integer displayOrder;
    @Column(name="qr_code_url", nullable=true, length=500) private String qrCodeUrl;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public EventArtwork() {
    }
    public Long getEventArtworkId() {
        return eventArtworkId;
    }
    public void setEventArtworkId(Long value) {
        this.eventArtworkId=value;
    }
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long value) {
        this.eventId=value;
    }
    public Long getArtworkId() {
        return artworkId;
    }
    public void setArtworkId(Long value) {
        this.artworkId=value;
    }
    public String getExhibitionArea() {
        return exhibitionArea;
    }
    public void setExhibitionArea(String value) {
        this.exhibitionArea=value;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer value) {
        this.displayOrder=value;
    }
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    public void setQrCodeUrl(String value) {
        this.qrCodeUrl=value;
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
