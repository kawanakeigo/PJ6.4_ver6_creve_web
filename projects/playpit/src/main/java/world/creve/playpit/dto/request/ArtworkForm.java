package world.creve.playpit.dto.request;
import jakarta.validation.constraints.*;
import java.time.*;
import org.springframework.format.annotation.DateTimeFormat;
public class ArtworkForm {
    @NotNull @Positive private Long creatorId;
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long value) {
        creatorId=value;
    }
    @NotBlank @Size(max=100) private String slug;
    public String getSlug() {
        return slug;
    }
    public void setSlug(String value) {
        slug=value;
    }
    @NotBlank @Size(max=150) private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String value) {
        title=value;
    }
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        description=value;
    }
    private String background;
    public String getBackground() {
        return background;
    }
    public void setBackground(String value) {
        background=value;
    }
    private String concept;
    public String getConcept() {
        return concept;
    }
    public void setConcept(String value) {
        concept=value;
    }
    @Size(max=300) private String materials;
    public String getMaterials() {
        return materials;
    }
    public void setMaterials(String value) {
        materials=value;
    }
    @Min(0) @Max(32767) private Short productionYear;
    public Short getProductionYear() {
        return productionYear;
    }
    public void setProductionYear(Short value) {
        productionYear=value;
    }
    @Size(max=500) private String mainImageUrl;
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    public void setMainImageUrl(String value) {
        mainImageUrl=value;
    }
    @NotBlank @Pattern(regexp="DRAFT|PUBLISHED|HIDDEN|ARCHIVED") private String status;
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        status=value;
    }
    private String mediaLines;
    public String getMediaLines() {
        return mediaLines;
    }
    public void setMediaLines(String value) {
        mediaLines=value;
    }
}
