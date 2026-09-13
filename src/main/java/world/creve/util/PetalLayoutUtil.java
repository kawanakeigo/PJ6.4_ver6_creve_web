package world.creve.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import org.springframework.stereotype.Component;

@Component
public class PetalLayoutUtil {
  public long createSeed(
      String eventId,
      String creatorId,
      String artworkId,
      String body,
      String sessionHash,
      long sequence
  ) {
    CRC32 crc32 = new CRC32();
    String value = eventId + ":" + creatorId + ":" + artworkId + ":" + body + ":" + sessionHash + ":" + sequence;
    crc32.update(value.getBytes(StandardCharsets.UTF_8));
    return crc32.getValue();
  }

  public int calculateGrowthStage(long totalMessages) {
    if (totalMessages >= 61) {
      return 4;
    }
    if (totalMessages >= 31) {
      return 3;
    }
    if (totalMessages >= 11) {
      return 2;
    }
    if (totalMessages >= 1) {
      return 1;
    }
    return 0;
  }

  public int petalType(long seed) {
    return (int) (Math.floorMod(seed, 5) + 1);
  }

  public double x(long seed, long index, int growthStage) {
    double radius = radius(index, growthStage);
    double angle = Math.toRadians(angle(seed, index));
    return clamp(50 + Math.cos(angle) * radius * 1.05);
  }

  public double y(long seed, long index, int growthStage) {
    double radius = radius(index, growthStage);
    double angle = Math.toRadians(angle(seed, index));
    return clamp(50 + Math.sin(angle) * radius * 0.72);
  }

  public double angle(long seed, long index) {
    return Math.floorMod(seed + index * 37, 360);
  }

  public double size(long seed, int growthStage) {
    return 0.82 + (Math.floorMod(seed, 24) / 100.0) + growthStage * 0.035;
  }

  private double radius(long index, int growthStage) {
    return Math.min(42, 10 + index * 0.58 + growthStage * 3.5);
  }

  private double clamp(double value) {
    return Math.max(7, Math.min(93, value));
  }
}
