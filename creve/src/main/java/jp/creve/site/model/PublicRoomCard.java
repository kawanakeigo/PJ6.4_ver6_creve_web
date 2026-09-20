package jp.creve.site.model;

import java.time.Instant;
import java.util.UUID;

public record PublicRoomCard(
        UUID roomId,
        String slug,
        String creatorName,
        String heading,
        String intro,
        String theme,
        Instant publishedAt) {
}
