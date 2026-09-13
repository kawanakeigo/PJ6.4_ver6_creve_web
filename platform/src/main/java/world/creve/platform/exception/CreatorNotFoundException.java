package world.creve.platform.exception;
public class CreatorNotFoundException extends RuntimeException {
    public CreatorNotFoundException() {
        super("クリエイターが見つかりません。");
    }
    public CreatorNotFoundException(String message) {
        super(message);
    }
}
