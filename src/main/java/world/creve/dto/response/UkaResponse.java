package world.creve.dto.response;

import java.util.List;

public record UkaResponse(
    String eventId,
    String eventSlug,
    long totalMessages,
    int growthStage,
    List<PetalResponse> petals
) {
}
