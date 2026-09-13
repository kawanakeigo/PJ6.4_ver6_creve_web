package world.creve.lp.dto;
import java.time.*;
import java.util.*;
public record LandingPageResponse(String slug, String title, String body, String ctaLabel, String ctaUrl) {
    public String getSlug() {
        return slug;
    }
    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }
    public String getCtaLabel() {
        return ctaLabel;
    }
    public String getCtaUrl() {
        return ctaUrl;
    }
}
