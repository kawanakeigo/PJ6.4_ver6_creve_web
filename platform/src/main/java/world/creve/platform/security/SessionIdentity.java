package world.creve.platform.security;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
@Component @RequestScope public class SessionIdentity {
    private final HttpServletRequest request;
    private final IdentityHasher hasher;
    public SessionIdentity(HttpServletRequest request,IdentityHasher hasher) {
        this.request=request;
        this.hasher=hasher;
    }
    public String hash() {
        return hasher.hash("session:"+request.getSession(true).getId());
    }
    public String idempotencyKey() {
        String raw=request.getHeader("Idempotency-Key");
        if(raw==null||raw.isBlank())return null;
        if(raw.length()>128||!raw.matches("[a-zA-Z0-9_-]+"))throw new world.creve.platform.exception.InvalidMessageException();
        return hasher.hash("request:"+raw);
    }
}
