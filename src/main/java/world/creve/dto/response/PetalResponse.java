package world.creve.dto.response;

import java.time.OffsetDateTime;
import world.creve.entity.Message;

public record PetalResponse(
    Long id,
    String eventId,
    String creatorId,
    String artworkId,
    String body,
    String displayName,
    OffsetDateTime createdAt,
    int petalType,
    long petalSeed,
    double petalX,
    double petalY,
    double petalAngle,
    double petalSize
) {
  public static PetalResponse from(Message message) {
    return new PetalResponse(
        message.getId(),
        message.getEventId(),
        message.getCreatorId(),
        message.getArtworkId(),
        message.getBody(),
        message.getDisplayName(),
        message.getCreatedAt(),
        message.getPetalType(),
        message.getPetalSeed(),
        message.getPetalX(),
        message.getPetalY(),
        message.getPetalAngle(),
        message.getPetalSize()
    );
  }
}
