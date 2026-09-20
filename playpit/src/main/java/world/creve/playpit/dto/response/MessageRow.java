package world.creve.playpit.dto.response;

import java.time.LocalDateTime;

public record MessageRow(
        Long messageId,
        Long creatorId,
        Long artworkId,
        String message,
        String publicName,
        LocalDateTime createdAt,
        String artworkTitle,
        String creatorName) {
}
