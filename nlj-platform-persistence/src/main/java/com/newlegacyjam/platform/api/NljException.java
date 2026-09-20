package com.newlegacyjam.platform.api;

public abstract class NljException extends RuntimeException {
    private final int status;
    private final String code;

    protected NljException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int status() { return status; }
    public String code() { return code; }

    public static final class NotFound extends NljException {
        public NotFound() { super(404, "NOT_FOUND", "対象が見つかりません。"); }
    }
    public static final class InvalidInput extends NljException {
        public InvalidInput(String code) { super(400, code, "入力内容を確認してください。"); }
    }
    public static final class Unauthorized extends NljException {
        public Unauthorized(String code) { super(401, code, "LINEで本人確認してください。"); }
    }
    public static final class Conflict extends NljException {
        public Conflict(String code, String message) { super(409, code, message); }
    }
    public static final class Unavailable extends NljException {
        public Unavailable(String code, String message) { super(503, code, message); }
    }
}
