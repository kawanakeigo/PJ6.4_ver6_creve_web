package world.creve.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import world.creve.exception.BadRequestException;

@Component
public class PostingRateLimiter {
  private static final int MAX_POSTS_PER_MINUTE = 5;
  private static final long WINDOW_SECONDS = 60;
  private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

  public void check(String sessionHash, String eventId, String creatorId, String artworkId) {
    String key = sessionHash + ":" + eventId + ":" + nullToDash(creatorId) + ":" + nullToDash(artworkId);
    Instant now = Instant.now();
    Deque<Instant> timestamps = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (timestamps) {
      while (!timestamps.isEmpty() && timestamps.peekFirst().plusSeconds(WINDOW_SECONDS).isBefore(now)) {
        timestamps.removeFirst();
      }
      if (timestamps.size() >= MAX_POSTS_PER_MINUTE) {
        throw new BadRequestException("短時間に投稿できる回数を超えました。少し時間をおいてください。");
      }
      timestamps.addLast(now);
    }
  }

  private String nullToDash(String value) {
    return value == null ? "-" : value;
  }
}
