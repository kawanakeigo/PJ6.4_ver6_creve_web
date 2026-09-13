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
    name = "event_artworks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "artwork_id"})
)
public class EventArtwork {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, length = 64)
  private String eventId;

  @Column(name = "artwork_id", nullable = false, length = 64)
  private String artworkId;

  @Column(nullable = false)
  private int displayOrder;

  protected EventArtwork() {
  }

  public EventArtwork(String eventId, String artworkId, int displayOrder) {
    this.eventId = eventId;
    this.artworkId = artworkId;
    this.displayOrder = displayOrder;
  }

  public Long getId() {
    return id;
  }

  public String getEventId() {
    return eventId;
  }

  public String getArtworkId() {
    return artworkId;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
