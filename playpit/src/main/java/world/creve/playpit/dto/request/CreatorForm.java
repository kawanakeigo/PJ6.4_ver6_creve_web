package world.creve.playpit.dto.request;
import jakarta.validation.constraints.*;
import java.time.*;
import org.springframework.format.annotation.DateTimeFormat;
public class CreatorForm {
    @NotBlank @Size(max=100) private String slug;
    public String getSlug() {
        return slug;
    }
    public void setSlug(String value) {
        slug=value;
    }
    @NotBlank @Size(max=100) private String name;
    public String getName() {
        return name;
    }
    public void setName(String value) {
        name=value;
    }
    @Size(max=100) private String nameKana;
    public String getNameKana() {
        return nameKana;
    }
    public void setNameKana(String value) {
        nameKana=value;
    }
    private String profile;
    public String getProfile() {
        return profile;
    }
    public void setProfile(String value) {
        profile=value;
    }
    private String concept;
    public String getConcept() {
        return concept;
    }
    public void setConcept(String value) {
        concept=value;
    }
    @Size(max=100) private String genre;
    public String getGenre() {
        return genre;
    }
    public void setGenre(String value) {
        genre=value;
    }
    @Size(max=500) private String profileImageUrl;
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String value) {
        profileImageUrl=value;
    }
    @Size(max=500) private String websiteUrl;
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    public void setWebsiteUrl(String value) {
        websiteUrl=value;
    }
    @NotBlank @Pattern(regexp="DRAFT|PUBLISHED|HIDDEN|ARCHIVED") private String status;
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        status=value;
    }
    @Size(max=5000) private String snsLinks;
    public String getSnsLinks() {
        return snsLinks;
    }
    public void setSnsLinks(String value) {
        snsLinks=value;
    }
}
