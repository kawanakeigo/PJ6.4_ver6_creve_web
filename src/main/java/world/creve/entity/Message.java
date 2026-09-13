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
@Table(name = "messages")
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, length = 64)
  private String eventId;

  @Column(name = "creator_id", length = 64)
  private String creatorId;

  @Column(name = "artwork_id", length = 64)
  private String artworkId;

  @Column(nullable = false, length = 300)
  private String body;

  @Column(name = "display_name", nullable = false, length = 30)
  private String displayName;

  @Column(nullable = false)
  private boolean anonymous;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MessageStatus status;

  @Column(name = "petal_type", nullable = false)
  private int petalType;

  @Column(name = "petal_seed", nullable = false)
  private long petalSeed;

  @Column(name = "petal_x", nullable = false)
  private double petalX;

  @Column(name = "petal_y", nullable = false)
  private double petalY;

  @Column(name = "petal_angle", nullable = false)
  private double petalAngle;

  @Column(name = "petal_size", nullable = false)
  private double petalSize;

  @Column(name = "session_hash", nullable = false, length = 128)
  private String sessionHash;

  protected Message() {
  }

  public Message(
      String eventId,
      String creatorId,
      String artworkId,
      String body,
      String displayName,
      boolean anonymous,
      MessageStatus status,
      int petalType,
      long petalSeed,
      double petalX,
      double petalY,
      double petalAngle,
      double petalSize,
      String sessionHash
  ) {
    this.eventId = eventId;
    this.creatorId = creatorId;
    this.artworkId = artworkId;
    this.body = body;
    this.displayName = displayName;
    this.anonymous = anonymous;
    this.createdAt = OffsetDateTime.now();
    this.status = status;
    this.petalType = petalType;
    this.petalSeed = petalSeed;
    this.petalX = petalX;
    this.petalY = petalY;
    this.petalAngle = petalAngle;
    this.petalSize = petalSize;
    this.sessionHash = sessionHash;
  }

  public Long getId() {
    return id;
  }

  public String getEventId() {
    return eventId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getArtworkId() {
    return artworkId;
  }

  public String getBody() {
    return body;
  }

  public String getDisplayName() {
    return anonymous ? "匿名" : displayName;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public MessageStatus getStatus() {
    return status;
  }

  public int getPetalType() {
    return petalType;
  }

  public long getPetalSeed() {
    return petalSeed;
  }

  public double getPetalX() {
    return petalX;
  }

  public double getPetalY() {
    return petalY;
  }

  public double getPetalAngle() {
    return petalAngle;
  }

  public double getPetalSize() {
    return petalSize;
  }

  public String getSessionHash() {
    return sessionHash;
  }

  public void publish() {
    this.status = MessageStatus.PUBLISHED;
  }

  public void hide() {
    this.status = MessageStatus.HIDDEN;
  }

  public void markDeleted() {
    this.status = MessageStatus.DELETED;
  }
}
