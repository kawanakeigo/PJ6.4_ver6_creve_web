package world.creve.playpit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
import world.creve.playpit.util.PetalLayout;
import world.creve.playpit.service.PostingPolicy;
import world.creve.playpit.validation.MessagePolicy;
import world.creve.playpit.entity.MessageStatus;
import world.creve.platform.exception.*;
class DesignPolicyTest {
    @ParameterizedTest @CsvSource( {
        "0,0","1,1","10,1","11,2","30,2","31,3","60,3","61,4","300,4","301,4"
    }
    ) void growth(long count,int stage) {
        assertEquals(stage,PetalLayout.growth(count));
    }
    @Test void negativeCountRejected() {
        assertThrows(IllegalArgumentException.class,()->PetalLayout.growth(-1));
    }
    @ParameterizedTest @ValueSource(longs= {
        0,1,2
    }
    ) void firstThreeAllowed(long existing) {
        assertDoesNotThrow(()->PostingPolicy.check(existing,false));
    }
    @ParameterizedTest @ValueSource(longs= {
        3,4,5
    }
    ) void fourthAndLaterRejected(long existing) {
        assertThrows(RateLimitExceededException.class,()->PostingPolicy.check(existing,false));
    }
    @Test void duplicateRejected() {
        assertThrows(RateLimitExceededException.class,()->PostingPolicy.check(0,true));
    }
    @Test void windows() {
        assertEquals(Duration.ofSeconds(60),PostingPolicy.WINDOW);
        assertEquals(Duration.ofMinutes(10),PostingPolicy.DUPLICATE_WINDOW);
    }
    @ParameterizedTest @ValueSource(ints= {
        1,299,300
    }
    ) void acceptedLength(int n) {
        assertEquals(MessageStatus.PUBLISHED,MessagePolicy.evaluate("あ".repeat(n),"名前"));
    }
    @ParameterizedTest @ValueSource(strings= {
        ""," ","\t\n","　"
    }
    ) void emptyRejected(String s) {
        assertThrows(InvalidMessageException.class,()->MessagePolicy.evaluate(s,null));
    }
    @Test void maxAndNull() {
        assertThrows(InvalidMessageException.class,()->MessagePolicy.evaluate("あ".repeat(301),null));
        assertThrows(InvalidMessageException.class,()->MessagePolicy.evaluate(null,null));
        assertThrows(InvalidMessageException.class,()->MessagePolicy.evaluate("感想","名".repeat(31)));
        assertDoesNotThrow(()->MessagePolicy.evaluate("感想","名".repeat(30)));
    }
    @ParameterizedTest @ValueSource(strings= {
        "<script>alert(1)</script>","https://example.invalid","死ね"
    }
    ) void forbidden(String s) {
        assertThrows(InvalidMessageException.class,()->MessagePolicy.evaluate(s,null));
    }
    @ParameterizedTest @ValueSource(strings= {
        "連絡はa@example.invalid","電話番号です","090-1234-5678"
    }
    )void reviewRequired(String s) {
        assertEquals(MessageStatus.PENDING,MessagePolicy.evaluate(s,null));
    }
    @Test void stableDistinctFeatherPositions() {
        Set<String> points=new HashSet<>();
        for(long i=1;i<=5000;i++) {
            long seed=PetalLayout.seed(i,Math.max(1,PetalLayout.growth(i)));
            var p=PetalLayout.position(seed);
            assertEquals(p,PetalLayout.position(seed));
            assertTrue(p.x()>0&&p.x()<100&&p.y()>0&&p.y()<100);
            assertTrue(points.add(Double.toHexString(p.x())+":"+Double.toHexString(p.y())));
            assertTrue(PetalLayout.type(seed)>=1&&PetalLayout.type(seed)<=5);
        }
    }
}
