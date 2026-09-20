package world.creve.platform.exception;
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException() {
        super("イベントが見つかりません。");
    }
    public EventNotFoundException(String message) {
        super(message);
    }
}
