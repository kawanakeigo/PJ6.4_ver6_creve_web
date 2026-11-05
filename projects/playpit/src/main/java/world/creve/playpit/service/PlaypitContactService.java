package world.creve.playpit.service;
import java.nio.file.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service public class PlaypitContactService {
    private final Path file;
    public PlaypitContactService(@Value("${playpit.contact-file:./config/public/playpit/contact.txt}")String file) {this.file=Path.of(file).toAbsolutePath().normalize();}
    @Value("${playpit.contact-form-url:}") private String formUrl="";
    public String formUrl() {return validatedFormUrl(formUrl);}
    public static String validatedFormUrl(String value) {
        if(value==null||value.isBlank())return null;
        try {
            var uri=java.net.URI.create(value.trim());
            if(!"https".equals(uri.getScheme())||uri.getUserInfo()!=null||uri.getPort()!=-1)return null;
            if(("docs.google.com".equals(uri.getHost())&&uri.getPath().startsWith("/forms/"))||("forms.gle".equals(uri.getHost())&&uri.getPath().length()>1))return uri.toString();
        }catch(IllegalArgumentException ignored){}
        return null;
    }
    public String content() {
        try {
            if(Files.isRegularFile(file)&&Files.size(file)<=1_000_000) {
                String text=Files.readString(file);return text.isBlank()?null:text;
            }
        } catch(IOException ignored) {}
        return null;
    }
}
