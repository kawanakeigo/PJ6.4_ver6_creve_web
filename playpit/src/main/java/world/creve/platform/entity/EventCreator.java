package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="event_creators") public class EventCreator {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="participation_id") private Long participationId;
    @Column(name="event_id", nullable=false) private Long eventId;
    @Column(name="creator_id", nullable=false) private Long creatorId;
    @Column(name="exhibition_title", nullable=true, length=150) private String exhibitionTitle;
    @Column(name="display_order", nullable=false) private Integer displayOrder;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public EventCreator() {
    }
    public Long getParticipationId() {
        return participationId;
    }
    public void setParticipationId(Long value) {
        this.participationId=value;
    }
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long value) {
        this.eventId=value;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long value) {
        this.creatorId=value;
    }
    public String getExhibitionTitle() {
        return exhibitionTitle;
    }
    public void setExhibitionTitle(String value) {
        this.exhibitionTitle=value;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer value) {
        this.displayOrder=value;
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
