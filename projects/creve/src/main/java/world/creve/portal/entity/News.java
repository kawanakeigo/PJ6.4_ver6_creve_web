package world.creve.portal.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="news") public class News {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="news_id") private Long newsId;
    @Column(name="slug", nullable=false, length=100, unique=true) private String slug;
    @Column(name="title", nullable=false, length=150) private String title;
    @Column(name="body", nullable=false, columnDefinition="TEXT") private String body;
    @Column(name="status", nullable=false, length=20) private String status;
    @Column(name="published_at", nullable=false) private LocalDateTime publishedAt;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public News() {
    }
    public Long getNewsId() {
        return newsId;
    }
    public void setNewsId(Long value) {
        this.newsId=value;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status=value;
    }
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(LocalDateTime value) {
        this.publishedAt=value;
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
