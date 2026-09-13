package world.creve.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageRequest {
  @NotBlank
  @Size(max = 64)
  private String eventId;

  @Size(max = 64)
  private String creatorId;

  @Size(max = 64)
  private String artworkId;

  @Size(max = 30)
  private String displayName;

  @NotBlank
  @Size(min = 1, max = 300)
  private String body;

  private boolean anonymous = true;

  @AssertTrue
  private boolean termsAgreed;

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public String getArtworkId() {
    return artworkId;
  }

  public void setArtworkId(String artworkId) {
    this.artworkId = artworkId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public boolean isTermsAgreed() {
    return termsAgreed;
  }

  public void setTermsAgreed(boolean termsAgreed) {
    this.termsAgreed = termsAgreed;
  }

  @AssertTrue(message = "投稿対象を指定してください。")
  public boolean hasTarget() {
    return hasText(creatorId) || hasText(artworkId);
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
