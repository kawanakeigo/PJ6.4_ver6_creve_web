package world.creve.playpit.dto.response;

public record MessageResponse(
        Long messageId,
        String status,
        String createdAt,
        String completionMessage) {
}
