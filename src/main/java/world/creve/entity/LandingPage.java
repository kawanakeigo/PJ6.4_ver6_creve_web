package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "landing_pages")
public class LandingPage {
  @Id
  @Column(length = 64)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PublishStatus status;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  protected LandingPage() {
  }

  public LandingPage(String id, String slug, PublishStatus status, String title, String body) {
    this.id = id;
    this.slug = slug;
    this.status = status;
    this.title = title;
    this.body = body;
  }

  public String getId() {
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
}
