package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="media_files") public class MediaFile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="media_id") private Long mediaId;
    @Column(name="artwork_id", nullable=false) private Long artworkId;
    @Column(name="media_type", nullable=false, length=20) private String mediaType;
    @Column(name="url", nullable=false, length=500) private String url;
    @Column(name="alt_text", nullable=true, length=300) private String altText;
    @Column(name="display_order", nullable=false) private Integer displayOrder;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public MediaFile() {
    }
    public Long getMediaId() {
        return mediaId;
    }
    public void setMediaId(Long value) {
        this.mediaId=value;
    }
    public Long getArtworkId() {
        return artworkId;
    }
    public void setArtworkId(Long value) {
        this.artworkId=value;
    }
    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String value) {
        this.mediaType=value;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String value) {
        this.url=value;
    }
    public String getAltText() {
        return altText;
    }
    public void setAltText(String value) {
        this.altText=value;
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
