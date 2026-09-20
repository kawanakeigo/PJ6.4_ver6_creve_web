package world.creve.playpit.validation;
import java.text.Normalizer;
import java.util.*;
import world.creve.playpit.entity.MessageStatus;
import world.creve.platform.exception.InvalidMessageException;
/** Minimal literal filter retained from the submitted project, plus PII review patterns. */
public final class MessagePolicy {
    private MessagePolicy() {
    }
    public static MessageStatus evaluate(String body,String displayName) {
        if(body==null||body.isBlank()||body.length()>300||(displayName!=null&&displayName.length()>30))throw new InvalidMessageException();
        String text=Normalizer.normalize(body+" "+(displayName==null?"":displayName),Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        for(String word:List.of("死ね","殺す","<script","javascript:","http://","https://"))if(text.contains(word))throw new InvalidMessageException("投稿できない表現が含まれています。");
        if(text.matches("(?s).*[^\\s@]+@[^\\s@]+\\.[^\\s@]+.*")||text.matches("(?s).*0[0-9-]{8,14}.*"))return MessageStatus.PENDING;
        for(String word:List.of("住所","電話番号","個人情報","メールアドレス"))if(text.contains(word))return MessageStatus.PENDING;
        return MessageStatus.PUBLISHED;
    }
}
