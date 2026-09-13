package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record ArtworkDetailResponse(Long artworkId, String slug, String title, String description, String background, String concept, String materials, Short productionYear, String mainImageUrl, Long creatorId, String creatorSlug, String creatorName, List<MediaResponse> media) {
    public Long getArtworkId() {
        return artworkId;
    }
    public String getSlug() {
        return slug;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getBackground() {
        return background;
    }
    public String getConcept() {
        return concept;
    }
    public String getMaterials() {
        return materials;
    }
    public Short getProductionYear() {
        return productionYear;
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
    public List<MediaResponse> getMedia() {
        return media;
    }
}
