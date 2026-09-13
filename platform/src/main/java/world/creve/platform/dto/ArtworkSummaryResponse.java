package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record ArtworkSummaryResponse(Long artworkId, String slug, String title, String mainImageUrl, Long creatorId, String creatorSlug, String creatorName) {
    public Long getArtworkId() {
        return artworkId;
    }
    public String getSlug() {
        return slug;
    }
    public String getTitle() {
        return title;
    }
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public String getCreatorSlug() {
        return creatorSlug;
    }
    public String getCreatorName() {
        return creatorName;
    }
}
