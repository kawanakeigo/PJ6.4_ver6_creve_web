package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artworks")
public class Artwork {
  @Id
  @Column(length = 64)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Column(name = "event_id", nullable = false, length = 64)
  private String eventId;

  @Column(name = "creator_id", nullable = false, length = 64)
  private String creatorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PublishStatus status;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "sub_image_url", nullable = false, columnDefinition = "TEXT")
  private String subImageUrl;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String background;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String intention;

  @Column(nullable = false, length = 120)
  private String technique;

  @Column(name = "production_year", nullable = false, length = 40)
  private String productionYear;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  protected Artwork() {
  }

  public Artwork(
      String id,
      String slug,
      String eventId,
      String creatorId,
      PublishStatus status,
      String title,
      String description,
      String imageUrl,
      String subImageUrl,
      String background,
      String intention,
      String technique,
      String productionYear,
      int displayOrder
  ) {
    this.id = id;
    this.slug = slug;
    this.eventId = eventId;
    this.creatorId = creatorId;
    this.status = status;
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.subImageUrl = subImageUrl;
    this.background = background;
    this.intention = intention;
    this.technique = technique;
    this.productionYear = productionYear;
    this.displayOrder = displayOrder;
  }

  public String getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public String getEventId() {
    return eventId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public PublishStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getSubImageUrl() {
    return subImageUrl;
  }

  public String getBackground() {
    return background;
  }

  public String getIntention() {
    return intention;
  }

  public String getTechnique() {
    return technique;
  }

  public String getProductionYear() {
    return productionYear;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
