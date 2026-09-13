package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_files")
public class MediaFile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "owner_type", nullable = false, length = 40)
  private String ownerType;

  @Column(name = "owner_id", nullable = false, length = 64)
  private String ownerId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String url;

  @Column(name = "alt_text", nullable = false, length = 160)
  private String altText;

  @Column(name = "media_type", nullable = false, length = 40)
  private String mediaType;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  protected MediaFile() {
  }

  public MediaFile(String ownerType, String ownerId, String url, String altText, String mediaType, int displayOrder) {
    this.ownerType = ownerType;
    this.ownerId = ownerId;
    this.url = url;
    this.altText = altText;
    this.mediaType = mediaType;
    this.displayOrder = displayOrder;
  }

  public Long getId() {
    return id;
  }

  public String getOwnerType() {
    return ownerType;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public String getUrl() {
    return url;
  }

  public String getAltText() {
    return altText;
  }

  public String getMediaType() {
    return mediaType;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
