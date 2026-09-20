package world.creve.platform.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("同じ受付キーに異なる内容は送信できません。");
    }
}
