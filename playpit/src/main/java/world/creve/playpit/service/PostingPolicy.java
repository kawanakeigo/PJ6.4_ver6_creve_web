package world.creve.playpit.service;
import java.time.*;
import world.creve.platform.exception.RateLimitExceededException;
public final class PostingPolicy {
    public static final int MAX_POSTS_PER_MINUTE=3;
    public static final Duration WINDOW=Duration.ofSeconds(60), DUPLICATE_WINDOW=Duration.ofMinutes(10);
    private PostingPolicy() {
    }
    public static void check(long recentCount,boolean duplicate) {
        if(recentCount>=MAX_POSTS_PER_MINUTE||duplicate)throw new RateLimitExceededException();
    }
}
