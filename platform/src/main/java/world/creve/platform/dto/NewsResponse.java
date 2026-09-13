package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record NewsResponse(Long newsId, String slug, String title, String body, LocalDateTime publishedAt) {
    public Long getNewsId() {
        return newsId;
    }
    public String getSlug() {
        return slug;
    }
    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
