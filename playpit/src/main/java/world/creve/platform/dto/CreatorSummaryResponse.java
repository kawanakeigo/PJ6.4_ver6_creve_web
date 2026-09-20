package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record CreatorSummaryResponse(Long creatorId, String slug, String name, String profileImageUrl, String genre, String profile, long artworkCount) {
    public Long getCreatorId() {
        return creatorId;
    }
    public String getSlug() {
        return slug;
    }
    public String getName() {
        return name;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public String getGenre() {
        return genre;
    }
    public String getProfile() {
        return profile;
    }
    public long getArtworkCount() {
        return artworkCount;
    }
}
