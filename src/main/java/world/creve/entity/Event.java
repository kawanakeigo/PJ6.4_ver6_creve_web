package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
public class Event {
  @Id
  @Column(length = 64)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 24)
  private EventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PublishStatus status;

  @Column(nullable = false, length = 160)
  private String name;

  @Column(nullable = false, length = 180)
  private String subtitle;

  @Column(nullable = false, length = 240)
  private String summary;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "start_at", nullable = false)
  private OffsetDateTime startAt;

  @Column(name = "end_at", nullable = false)
  private OffsetDateTime endAt;

  @Column(name = "schedule_text", nullable = false, length = 120)
  private String scheduleText;

  @Column(name = "venue_name", nullable = false, length = 120)
  private String venueName;

  @Column(name = "venue_text", nullable = false, length = 160)
  private String venueText;

  @Column(name = "main_image_url", nullable = false, columnDefinition = "TEXT")
  private String mainImageUrl;

  @Column(nullable = false)
  private boolean postable;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  protected Event() {
  }

  public Event(
      String id,
      String slug,
      EventType eventType,
      PublishStatus status,
      String name,
      String subtitle,
      String summary,
      String description,
      OffsetDateTime startAt,
      OffsetDateTime endAt,
      String scheduleText,
      String venueName,
      String venueText,
      String mainImageUrl,
      boolean postable,
      int displayOrder
  ) {
    this.id = id;
    this.slug = slug;
    this.eventType = eventType;
    this.status = status;
    this.name = name;
    this.subtitle = subtitle;
    this.summary = summary;
    this.description = description;
    this.startAt = startAt;
    this.endAt = endAt;
    this.scheduleText = scheduleText;
    this.venueName = venueName;
    this.venueText = venueText;
    this.mainImageUrl = mainImageUrl;
    this.postable = postable;
    this.displayOrder = displayOrder;
  }

  public String getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public EventType getEventType() {
    return eventType;
  }

  public PublishStatus getStatus() {
    return status;
  }

  public String getName() {
    return name;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getSummary() {
    return summary;
  }

  public String getDescription() {
    return description;
  }

  public OffsetDateTime getStartAt() {
    return startAt;
  }

  public OffsetDateTime getEndAt() {
    return endAt;
  }

  public String getScheduleText() {
    return scheduleText;
  }

  public String getVenueName() {
    return venueName;
  }

  public String getVenueText() {
    return venueText;
  }

  public String getMainImageUrl() {
    return mainImageUrl;
  }

  public boolean isPostable() {
    return postable;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isPublished() {
    return status == PublishStatus.PUBLISHED;
  }
}
