package world.creve.platform.exception;
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("ログインが必要です。");
    }
    public UnauthorizedException(String message) {
        super(message);
    }
}
