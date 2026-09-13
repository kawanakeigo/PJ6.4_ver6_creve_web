package world.creve.platform.exception;
public class InvalidParticipationException extends RuntimeException {
    public InvalidParticipationException() {
        super("指定されたイベントとの所属関係がありません。");
    }
    public InvalidParticipationException(String message) {
        super(message);
    }
}
