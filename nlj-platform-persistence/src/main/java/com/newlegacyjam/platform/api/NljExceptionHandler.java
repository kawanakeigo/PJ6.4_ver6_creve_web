package com.newlegacyjam.platform.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NljExceptionHandler {
    @ExceptionHandler(NljException.class)
    public ResponseEntity<Map<String, Object>> known(NljException exception) {
        return ResponseEntity.status(exception.status()).body(Map.of(
                "code", exception.code(), "message", exception.getMessage(), "at", Instant.now().toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid() {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_INPUT", "message", "入力内容を確認してください。"));
    }
}
