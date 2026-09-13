package world.creve.platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import world.creve.platform.util.SafeUrls;
class UrlPolicyTest {
    @ParameterizedTest @ValueSource(strings= {
        "javascript:alert(1)","data:text/html,hello","//example.invalid","https://user:pass@example.invalid","/path\\bad","https://example.invalid\nxx"
    }
    )void rejectExecutableAndAmbiguous(String s) {
        assertThrows(IllegalArgumentException.class,()->SafeUrls.optional(s));
    }
    @ParameterizedTest @ValueSource(strings= {
        "/playpit/images/a.svg","https://example.invalid/art?a=1","http://localhost:8080/"
    }
    )void accept(String s) {
        assertEquals(s,SafeUrls.optional(s));
    }
    @Test void optional() {
        assertNull(SafeUrls.optional(null));
        assertNull(SafeUrls.optional(" "));
    }
    @Test void slug() {
        assertEquals("202609-playpit",SafeUrls.slug("202609-playpit"));
        assertThrows(IllegalArgumentException.class,()->SafeUrls.slug("../outside"));
    }
}
