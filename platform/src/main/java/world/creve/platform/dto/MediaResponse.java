package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record MediaResponse(String mediaType, String url, String altText) {
    public String getMediaType() {
        return mediaType;
    }
    public String getUrl() {
        return url;
    }
    public String getAltText() {
        return altText;
    }
}
