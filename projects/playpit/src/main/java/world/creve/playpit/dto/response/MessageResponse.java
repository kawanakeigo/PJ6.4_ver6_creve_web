package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record MessageResponse(Long messageId, String status, Integer petalType, Long petalSeed, String createdAt, String completionMessage) {
    public Long getMessageId() {
        return messageId;
    }
    public String getStatus() {
        return status;
    }
    public Integer getPetalType() {
        return petalType;
    }
    public Long getPetalSeed() {
        return petalSeed;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getCompletionMessage() {
        return completionMessage;
    }
}
