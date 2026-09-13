package world.creve.platform.exception;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
@ControllerAdvice public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler( {
        EventNotFoundException.class,CreatorNotFoundException.class,ArtworkNotFoundException.class,InvalidParticipationException.class
    }
    ) public Object missing(RuntimeException ex,HttpServletRequest req) {
        return response(404,"対象が見つかりません。",req);
    }
    @ExceptionHandler( {
        InvalidMessageException.class,MethodArgumentNotValidException.class,BindException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class
    }
    ) public Object invalid(Exception ex,HttpServletRequest req) {
        return response(400,"入力内容を確認してください。",req);
    }
    @ExceptionHandler(RateLimitExceededException.class) public Object rate(RateLimitExceededException ex,HttpServletRequest req) {
        return response(429,ex.getMessage(),req);
    }
    @ExceptionHandler(UnauthorizedException.class) public Object unauthorized(UnauthorizedException ex,HttpServletRequest req) {
        return response(401,ex.getMessage(),req);
    }
    @ExceptionHandler(ForbiddenException.class) public Object forbidden(ForbiddenException ex,HttpServletRequest req) {
        return response(403,ex.getMessage(),req);
    }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class) public Object method(Exception ex,HttpServletRequest req) {
        return response(405,"この操作方法は使用できません。",req);
    }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class) public Object noResource(Exception ex,HttpServletRequest req) {
        return response(404,"対象が見つかりません。",req);
    }
    @ExceptionHandler(Exception.class) public Object unexpected(Exception ex,HttpServletRequest req) {
        // Never log exception messages/SQL parameters: they may contain user text or credentials.
        log.error("operation_failed category={}",ex.getClass().getSimpleName());
        return response(500,"処理に失敗しました。時間をおいてから再度お試しください。",req);
    }
    private Object response(int status,String text,HttpServletRequest req) {
        log.info("request_rejected status={}",status);
        if(req.getRequestURI().startsWith("/api/")) return ResponseEntity.status(status).body(Map.of("status",status,"error",text));
        ModelAndView mv=new ModelAndView("common/error");
        mv.setStatus(org.springframework.http.HttpStatus.valueOf(status));
        mv.addObject("status",status);
        mv.addObject("errorMessage",text);
        return mv;
    }
}
