package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="creator_links") public class CreatorLink {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="link_id") private Long linkId;
    @Column(name="creator_id", nullable=false) private Long creatorId;
    @Column(name="label", nullable=false, length=80) private String label;
    @Column(name="url", nullable=false, length=500) private String url;
    @Column(name="display_order", nullable=false) private Integer displayOrder;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public CreatorLink() {
    }
    public Long getLinkId() {
        return linkId;
    }
    public void setLinkId(Long value) {
        this.linkId=value;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long value) {
        this.creatorId=value;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String value) {
        this.label=value;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String value) {
        this.url=value;
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
