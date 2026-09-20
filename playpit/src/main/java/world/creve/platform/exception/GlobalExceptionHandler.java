package world.creve.platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import world.creve.common.web.ApiError;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({EventNotFoundException.class, CreatorNotFoundException.class,
            ArtworkNotFoundException.class, InvalidParticipationException.class})
    public Object missing(RuntimeException exception, HttpServletRequest request) {
        return response(404, "NOT_FOUND", "対象が見つかりません。", List.of(), request);
    }

    @ExceptionHandler({InvalidMessageException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class})
    public Object invalid(Exception exception, HttpServletRequest request) {
        return response(400, "VALIDATION_ERROR", "入力内容を確認してください。", List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object invalidArgument(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), "入力内容を確認してください。"))
                .distinct().toList();
        return response(400, "VALIDATION_ERROR", "入力内容を確認してください。", fields, request);
    }

    @ExceptionHandler(BindException.class)
    public Object invalidBinding(BindException exception, HttpServletRequest request) {
        var fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), "入力内容を確認してください。"))
                .distinct().toList();
        return response(400, "VALIDATION_ERROR", "入力内容を確認してください。", fields, request);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public Object idempotency(IdempotencyConflictException exception, HttpServletRequest request) {
        return response(409, "IDEMPOTENCY_CONFLICT", exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public Object rate(RateLimitExceededException exception, HttpServletRequest request) {
        return response(429, "RATE_LIMITED", exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Object unauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return response(401, "UNAUTHORIZED", exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public Object forbidden(ForbiddenException exception, HttpServletRequest request) {
        return response(403, "FORBIDDEN", exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Object method(Exception exception, HttpServletRequest request) {
        return response(405, "METHOD_NOT_ALLOWED", "この操作方法は使用できません。", List.of(), request);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Object noResource(Exception exception, HttpServletRequest request) {
        return response(404, "NOT_FOUND", "対象が見つかりません。", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public Object unexpected(Exception exception, HttpServletRequest request) {
        return response(500, "INTERNAL_ERROR",
                "処理に失敗しました。時間をおいてから再度お試しください。", List.of(), request);
    }

    private Object response(
            int status, String code, String message, List<ApiError.FieldError> fields,
            HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        if (status >= 500) {
            log.error("operation_failed status={} code={} traceId={}", status, code, traceId);
        } else {
            log.info("request_rejected status={} code={} traceId={}", status, code, traceId);
        }
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(status).body(new ApiError(code, message, fields, traceId));
        }
        ModelAndView view = new ModelAndView("common/error");
        view.setStatus(HttpStatus.valueOf(status));
        view.addObject("status", status);
        view.addObject("errorMessage", message);
        return view;
    }
}
