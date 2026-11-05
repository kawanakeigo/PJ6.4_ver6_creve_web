package world.creve.playpit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import world.creve.playpit.dto.response.*;
class WordResponseTest {
 @Test void normalCommentsNeverExposeAnonymousNameOrFlowerGeometry(){
  var word=WordResponse.from(new MessageRow(1L,2L,3L,"感想","非公開の名前",true,1,9L,LocalDateTime.of(2026,11,1,12,0),"作品","作家"));
  assertEquals("匿名",word.displayName());assertEquals("感想",word.message());
  assertEquals(6,WordResponse.class.getRecordComponents().length);
 }
}
