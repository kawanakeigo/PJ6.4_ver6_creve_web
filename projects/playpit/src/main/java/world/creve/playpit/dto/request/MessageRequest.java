package world.creve.playpit.dto.request;
import jakarta.validation.constraints.*;
import world.creve.playpit.validation.ValidMessageTarget;
@ValidMessageTarget public class MessageRequest {
    @NotNull @Positive private Long eventId;
    @Positive private Long creatorId;
    @Positive private Long artworkId;
    @NotBlank @Size(max=300) private String message;
    @Size(max=30) private String displayName;
    @NotNull private Boolean anonymous;
    @NotNull @AssertTrue private Boolean agreement;
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long v) {
        eventId=v;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long v) {
        creatorId=v;
    }
    public Long getArtworkId() {
        return artworkId;
    }
    public void setArtworkId(Long v) {
        artworkId=v;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String v) {
        message=v;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String v) {
        displayName=v;
    }
    public Boolean getAnonymous() {
        return anonymous;
    }
    public void setAnonymous(Boolean v) {
        anonymous=v;
    }
    public Boolean getAgreement() {
        return agreement;
    }
    public void setAgreement(Boolean v) {
        agreement=v;
    }
}
