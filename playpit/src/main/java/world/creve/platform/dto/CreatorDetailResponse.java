package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record CreatorDetailResponse(Long creatorId, String slug, String name, String nameKana, String profile, String concept, String genre, String profileImageUrl, String websiteUrl, List<CreatorLinkResponse> snsLinks) {
    public Long getCreatorId() {
        return creatorId;
    }
    public String getSlug() {
        return slug;
    }
    public String getName() {
        return name;
    }
    public String getNameKana() {
        return nameKana;
    }
    public String getProfile() {
        return profile;
    }
    public String getConcept() {
        return concept;
    }
    public String getGenre() {
        return genre;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    public List<CreatorLinkResponse> getSnsLinks() {
        return snsLinks;
    }
}
