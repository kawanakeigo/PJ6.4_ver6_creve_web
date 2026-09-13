package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "creators")
public class Creator {
  @Id
  @Column(length = 64)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PublishStatus status;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "artist_name", length = 120)
  private String artistName;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String profile;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String concept;

  @Column(name = "icon_image_url", nullable = false, columnDefinition = "TEXT")
  private String iconImageUrl;

  @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
  private String imageUrl;

  @Column(length = 240)
  private String instagram;

  @Column(length = 240)
  private String x;

  @Column(length = 240)
  private String website;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  protected Creator() {
  }

  public Creator(
      String id,
      String slug,
      PublishStatus status,
      String name,
      String artistName,
      String category,
      String profile,
      String concept,
      String iconImageUrl,
      String imageUrl,
      String instagram,
      String x,
      String website,
      int displayOrder
  ) {
    this.id = id;
    this.slug = slug;
    this.status = status;
    this.name = name;
    this.artistName = artistName;
    this.category = category;
    this.profile = profile;
    this.concept = concept;
    this.iconImageUrl = iconImageUrl;
    this.imageUrl = imageUrl;
    this.instagram = instagram;
    this.x = x;
    this.website = website;
    this.displayOrder = displayOrder;
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

  public String getName() {
    return name;
  }

  public String getArtistName() {
    return artistName;
  }

  public String getCategory() {
    return category;
  }

  public String getProfile() {
    return profile;
  }

  public String getConcept() {
    return concept;
  }

  public String getIconImageUrl() {
    return iconImageUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getInstagram() {
    return instagram;
  }

  public String getX() {
    return x;
  }

  public String getWebsite() {
    return website;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
