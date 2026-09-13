package world.creve.platform.security;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class IdentityHasher {
    private final byte[] key;
    public IdentityHasher(@Value("${creve.session.hash-salt}")String secret) {
        if(secret==null||secret.length()<32)throw new IllegalStateException("CREVE_SESSION_HASH_SALT must be at least 32 characters");
        key=secret.getBytes(StandardCharsets.UTF_8);
    }
    public String hash(String value) {
        try {
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key,"HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch(java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC unavailable");
        }
    }
}
