package world.creve.platform.exception;
public class ArtworkNotFoundException extends RuntimeException {
    public ArtworkNotFoundException() {
        super("作品が見つかりません。");
    }
    public ArtworkNotFoundException(String message) {
        super(message);
    }
}
