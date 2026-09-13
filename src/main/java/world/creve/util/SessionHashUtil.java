package world.creve.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionHashUtil {
  private final String salt;

  public SessionHashUtil(@Value("${creve.session.hash-salt:creve-local-salt}") String salt) {
    this.salt = salt;
  }

  public String hash(String sessionId) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest((salt + ":" + sessionId).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available.", ex);
    }
  }
}
