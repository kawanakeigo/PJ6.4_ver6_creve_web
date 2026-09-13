package world.creve.dto.response;

import java.time.OffsetDateTime;
import world.creve.entity.Message;
import world.creve.entity.MessageStatus;

public record MessageResponse(
    Long id,
    String eventId,
    String creatorId,
    String artworkId,
    String body,
    String displayName,
    OffsetDateTime createdAt,
    MessageStatus status,
    int petalType,
    long petalSeed,
    double petalX,
    double petalY,
    double petalAngle,
    double petalSize
) {
  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getEventId(),
        message.getCreatorId(),
        message.getArtworkId(),
        message.getBody(),
        message.getDisplayName(),
        message.getCreatedAt(),
        message.getStatus(),
        message.getPetalType(),
        message.getPetalSeed(),
        message.getPetalX(),
        message.getPetalY(),
        message.getPetalAngle(),
        message.getPetalSize()
    );
  }
}
