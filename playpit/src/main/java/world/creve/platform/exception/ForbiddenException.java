package world.creve.platform.exception;
public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("この操作は許可されていません。");
    }
    public ForbiddenException(String message) {
        super(message);
    }
}
