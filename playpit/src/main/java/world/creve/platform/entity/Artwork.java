package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="artworks") public class Artwork {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="artwork_id") private Long artworkId;
    @Column(name="creator_id", nullable=false) private Long creatorId;
    @Column(name="slug", nullable=false, length=100, unique=true) private String slug;
    @Column(name="title", nullable=false, length=150) private String title;
    @Column(name="description", nullable=true, columnDefinition="TEXT") private String description;
    @Column(name="background", nullable=true, columnDefinition="TEXT") private String background;
    @Column(name="concept", nullable=true, columnDefinition="TEXT") private String concept;
    @Column(name="materials", nullable=true, length=300) private String materials;
    @Column(name="production_year", nullable=true) private Short productionYear;
    @Column(name="main_image_url", nullable=true, length=500) private String mainImageUrl;
    @Column(name="status", nullable=false, length=20) private String status;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public Artwork() {
    }
    public Long getArtworkId() {
        return artworkId;
    }
    public void setArtworkId(Long value) {
        this.artworkId=value;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long value) {
        this.creatorId=value;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description=value;
    }
    public String getBackground() {
        return background;
    }
    public void setBackground(String value) {
        this.background=value;
    }
    public String getConcept() {
        return concept;
    }
    public void setConcept(String value) {
        this.concept=value;
    }
    public String getMaterials() {
        return materials;
    }
    public void setMaterials(String value) {
        this.materials=value;
    }
    public Short getProductionYear() {
        return productionYear;
    }
    public void setProductionYear(Short value) {
        this.productionYear=value;
    }
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public void setMainImageUrl(String value) {
        this.mainImageUrl=value;
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
