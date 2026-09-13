package world.creve.portal.service;
import java.nio.file.*;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service public class PublicContentService {
    private final Path directory;
    public PublicContentService(@Value("${creve.public-content-dir}")String directory) {
        this.directory=Path.of(directory).toAbsolutePath().normalize();
    }
    public String content(String kind) {
        if(!Set.of("contact","privacy","terms").contains(kind))throw new IllegalArgumentException("Unknown content");
        Path file=directory.resolve(kind+".txt");
        try {
            if(Files.isRegularFile(file)&&Files.size(file)<=1_000_000)return Files.readString(file);
        }
        catch(IOException ignored) {
        }
        return null;
    }
}
