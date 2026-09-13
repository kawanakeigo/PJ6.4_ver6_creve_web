package world.creve.playpit.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="messages") public class Message {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="message_id") private Long messageId;
    @Column(name="event_id", nullable=false) private Long eventId;
    @Column(name="creator_id", nullable=true) private Long creatorId;
    @Column(name="artwork_id", nullable=true) private Long artworkId;
    @Column(name="message", nullable=false, length=300) private String message;
    @Column(name="display_name", nullable=true, length=30) private String displayName;
    @Column(name="is_anonymous", nullable=false) private Boolean anonymous;
    @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=20) private MessageStatus status;
    @Convert(converter=world.creve.playpit.entity.SmallIntegerConverter.class) @Column(name="petal_type", nullable=false) private Integer petalType;
    @Column(name="petal_seed", nullable=false) private Long petalSeed;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public Message() {
    }
    public Long getMessageId() {
        return messageId;
    }
    public void setMessageId(Long value) {
        this.messageId=value;
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
    public Long getArtworkId() {
        return artworkId;
    }
    public void setArtworkId(Long value) {
        this.artworkId=value;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String value) {
        this.message=value;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String value) {
        this.displayName=value;
    }
    public Boolean getAnonymous() {
        return anonymous;
    }
    public void setAnonymous(Boolean value) {
        this.anonymous=value;
    }
    public MessageStatus getStatus() {
        return status;
    }
    public void setStatus(MessageStatus value) {
        this.status=value;
    }
    public Integer getPetalType() {
        return petalType;
    }
    public void setPetalType(Integer value) {
        this.petalType=value;
    }
    public Long getPetalSeed() {
        return petalSeed;
    }
    public void setPetalSeed(Long value) {
        this.petalSeed=value;
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
