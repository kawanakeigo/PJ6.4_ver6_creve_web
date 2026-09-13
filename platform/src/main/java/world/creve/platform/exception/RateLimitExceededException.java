package world.creve.platform.exception;
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("投稿が集中しています。時間をおいてから再送してください。");
    }
    public RateLimitExceededException(String message) {
        super(message);
    }
}
