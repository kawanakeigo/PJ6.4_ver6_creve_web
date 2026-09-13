package world.creve.playpit;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import world.creve.playpit.dto.request.MessageRequest;
class MessageValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;
    @BeforeAll static void start() {
        factory=Validation.buildDefaultValidatorFactory();
        validator=factory.getValidator();
    }
    @AfterAll static void stop() {
        factory.close();
    }
    private MessageRequest valid() {
        var r=new MessageRequest();
        r.setEventId(1L);
        r.setCreatorId(2L);
        r.setMessage("感想");
        r.setAnonymous(true);
        r.setAgreement(true);
        return r;
    }
    @Test void validCreatorOnly() {
        assertTrue(validator.validate(valid()).isEmpty());
    }
    @Test void artworkOnly() {
        var r=valid();
        r.setCreatorId(null);
        r.setArtworkId(3L);
        assertTrue(validator.validate(r).isEmpty());
    }
    @Test void missingTarget() {
        var r=valid();
        r.setCreatorId(null);
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test void missingAnonymous() {
        var r=valid();
        r.setAnonymous(null);
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test void consentFalseOrNull() {
        var r=valid();
        r.setAgreement(false);
        assertFalse(validator.validate(r).isEmpty());
        r.setAgreement(null);
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test void bodyBoundaries() {
        var r=valid();
        r.setMessage("あ".repeat(300));
        assertTrue(validator.validate(r).isEmpty());
        r.setMessage("あ".repeat(301));
        assertFalse(validator.validate(r).isEmpty());
        r.setMessage("  ");
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test void negativeIds() {
        var r=valid();
        r.setEventId(-1L);
        assertFalse(validator.validate(r).isEmpty());
    }
}
