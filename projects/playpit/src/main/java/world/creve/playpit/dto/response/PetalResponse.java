package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record PetalResponse(Long messageId, Integer petalType, Double positionX, Double positionY, Double rotation, Double scale, String message, String displayName, String artworkTitle, String creatorName, String createdAt) {
    public Long getMessageId() {
        return messageId;
    }
    public Integer getPetalType() {
        return petalType;
    }
    public Double getPositionX() {
        return positionX;
    }
    public Double getPositionY() {
        return positionY;
    }
    public Double getRotation() {
        return rotation;
    }
    public Double getScale() {
        return scale;
    }
    public String getMessage() {
        return message;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getArtworkTitle() {
        return artworkTitle;
    }
    public String getCreatorName() {
        return creatorName;
    }
    public String getCreatedAt() {
        return createdAt;
    }
}
