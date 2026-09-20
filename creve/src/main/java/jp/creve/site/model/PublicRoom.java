package jp.creve.site.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicRoom(
        UUID roomId,
        UUID publicationId,
        String slug,
        String creatorName,
        String creatorProfile,
        String heading,
        String intro,
        String theme,
        Instant publishedAt,
        List<PublicWork> works) {
}
