package world.creve.lp.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="landing_pages") public class LandingPage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="landing_page_id") private Long landingPageId;
    @Column(name="slug", nullable=false, length=100, unique=true) private String slug;
    @Column(name="title", nullable=false, length=150) private String title;
    @Column(name="body", nullable=true, columnDefinition="TEXT") private String body;
    @Column(name="cta_label", nullable=true, length=100) private String ctaLabel;
    @Column(name="cta_url", nullable=true, length=500) private String ctaUrl;
    @Column(name="status", nullable=false, length=20) private String status;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public LandingPage() {
    }
    public Long getLandingPageId() {
        return landingPageId;
    }
    public void setLandingPageId(Long value) {
        this.landingPageId=value;
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
    public String getBody() {
        return body;
    }
    public void setBody(String value) {
        this.body=value;
    }
    public String getCtaLabel() {
        return ctaLabel;
    }
    public void setCtaLabel(String value) {
        this.ctaLabel=value;
    }
    public String getCtaUrl() {
        return ctaUrl;
    }
    public void setCtaUrl(String value) {
        this.ctaUrl=value;
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
