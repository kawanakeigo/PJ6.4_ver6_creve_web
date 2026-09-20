package world.creve.platform.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
@Entity @Table(name="admins") public class Admin {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="admin_id") private Long adminId;
    @Column(name="email", nullable=false, length=254, unique=true) private String email;
    @Column(name="password_hash", nullable=false, length=255) private String passwordHash;
    @Column(name="enabled", nullable=false) private Boolean enabled;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
    public Admin() {
    }
    public Long getAdminId() {
        return adminId;
    }
    public void setAdminId(Long value) {
        this.adminId=value;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String value) {
        this.email=value;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String value) {
        this.passwordHash=value;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean value) {
        this.enabled=value;
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
