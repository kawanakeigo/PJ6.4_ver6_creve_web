package world.creve.validation;

import java.util.List;
import org.springframework.stereotype.Component;
import world.creve.entity.MessageStatus;
import world.creve.exception.BadRequestException;

@Component
public class MessageValidator {
  private static final List<String> BANNED_WORDS = List.of(
      "死ね",
      "殺す",
      "<script",
      "javascript:",
      "http://",
      "https://"
  );
  private static final List<String> REVIEW_WORDS = List.of(
      "住所",
      "電話番号",
      "個人情報",
      "メールアドレス"
  );

  public MessageStatus validateAndDecideStatus(String body, String displayName) {
    validateBody(body);
    validateDisplayName(displayName);
    String normalized = body.trim().toLowerCase();
    for (String bannedWord : BANNED_WORDS) {
      if (normalized.contains(bannedWord.toLowerCase())) {
        throw new BadRequestException("投稿できない表現が含まれています。");
      }
    }
    for (String reviewWord : REVIEW_WORDS) {
      if (normalized.contains(reviewWord.toLowerCase())) {
        return MessageStatus.PENDING;
      }
    }
    return MessageStatus.PUBLISHED;
  }

  private void validateBody(String body) {
    if (body == null || body.trim().isEmpty()) {
      throw new BadRequestException("メッセージを入力してください。");
    }
    if (body.trim().length() > 300) {
      throw new BadRequestException("メッセージは300文字以内で入力してください。");
    }
  }

  private void validateDisplayName(String displayName) {
    if (displayName != null && displayName.trim().length() > 30) {
      throw new BadRequestException("表示名は30文字以内で入力してください。");
    }
  }
}
