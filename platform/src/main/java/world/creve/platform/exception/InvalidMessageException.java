package world.creve.platform.exception;
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException() {
        super("入力内容を確認してください。");
    }
    public InvalidMessageException(String message) {
        super(message);
    }
}
