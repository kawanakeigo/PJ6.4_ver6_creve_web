package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="creators") public class Creator {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="creator_id") private Long creatorId;
    @Column(name="slug", nullable=false, length=100, unique=true) private String slug;
    @Column(name="name", nullable=false, length=100) private String name;
    @Column(name="name_kana", nullable=true, length=100) private String nameKana;
    @Column(name="profile", nullable=true, columnDefinition="TEXT") private String profile;
    @Column(name="concept", nullable=true, columnDefinition="TEXT") private String concept;
    @Column(name="genre", nullable=true, length=100) private String genre;
    @Column(name="profile_image_url", nullable=true, length=500) private String profileImageUrl;
    @Column(name="website_url", nullable=true, length=500) private String websiteUrl;
    @Column(name="status", nullable=false, length=20) private String status;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public Creator() {
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
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name=value;
    }
    public String getNameKana() {
        return nameKana;
    }
    public void setNameKana(String value) {
        this.nameKana=value;
    }
    public String getProfile() {
        return profile;
    }
    public void setProfile(String value) {
        this.profile=value;
    }
    public String getConcept() {
        return concept;
    }
    public void setConcept(String value) {
        this.concept=value;
    }
    public String getGenre() {
        return genre;
    }
    public void setGenre(String value) {
        this.genre=value;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String value) {
        this.profileImageUrl=value;
    }
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    public void setWebsiteUrl(String value) {
        this.websiteUrl=value;
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
