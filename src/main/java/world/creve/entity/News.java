package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "news")
public class News {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PublishStatus status;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  @Column(name = "published_at", nullable = false)
  private OffsetDateTime publishedAt;

  protected News() {
  }

  public News(String slug, PublishStatus status, String title, String body, OffsetDateTime publishedAt) {
    this.slug = slug;
    this.status = status;
    this.title = title;
    this.body = body;
    this.publishedAt = publishedAt;
  }

  public Long getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public PublishStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public OffsetDateTime getPublishedAt() {
    return publishedAt;
  }
}
