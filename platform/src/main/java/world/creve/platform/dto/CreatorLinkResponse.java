package world.creve.platform.dto;
import java.time.*;
import java.util.*;
public record CreatorLinkResponse(String label, String url) {
    public String getLabel() {
        return label;
    }
    public String getUrl() {
        return url;
    }
}
