package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record MessageRow(Long messageId, Long creatorId, Long artworkId, String message, String displayName, Boolean anonymous, Integer petalType, Long petalSeed, LocalDateTime createdAt, String artworkTitle, String creatorName) {
    public Long getMessageId() {
        return messageId;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public Long getArtworkId() {
        return artworkId;
    }
    public String getMessage() {
        return message;
    }
    public String getDisplayName() {
        return displayName;
    }
    public Boolean getAnonymous() {
        return anonymous;
    }
    public Integer getPetalType() {
        return petalType;
    }
    public Long getPetalSeed() {
        return petalSeed;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public String getArtworkTitle() {
        return artworkTitle;
    }
    public String getCreatorName() {
        return creatorName;
    }
    public String publicName() {
        return Boolean.TRUE.equals(anonymous)||displayName==null||displayName.isBlank()?"匿名":displayName;
    }
}
