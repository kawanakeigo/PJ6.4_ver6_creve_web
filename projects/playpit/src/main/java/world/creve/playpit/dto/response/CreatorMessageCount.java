package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record CreatorMessageCount(Long creatorId, String creatorSlug, String creatorName, Long messageCount) {
    public Long getCreatorId() {
        return creatorId;
    }
    public String getCreatorSlug() {
        return creatorSlug;
    }
    public String getCreatorName() {
        return creatorName;
    }
    public Long getMessageCount() {
        return messageCount;
    }
}
