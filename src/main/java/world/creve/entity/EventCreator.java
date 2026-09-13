package world.creve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "event_creators",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "creator_id"})
)
public class EventCreator {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, length = 64)
  private String eventId;

  @Column(name = "creator_id", nullable = false, length = 64)
  private String creatorId;

  @Column(nullable = false)
  private int displayOrder;

  protected EventCreator() {
  }

  public EventCreator(String eventId, String creatorId, int displayOrder) {
    this.eventId = eventId;
    this.creatorId = creatorId;
    this.displayOrder = displayOrder;
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

  public int getDisplayOrder() {
    return displayOrder;
  }
}
