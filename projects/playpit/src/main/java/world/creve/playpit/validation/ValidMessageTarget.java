package world.creve.playpit.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Constraint(validatedBy=MessageTargetValidator.class) public @interface ValidMessageTarget {
    String message() default "クリエイターまたは作品を指定してください。";
    Class<?>[] groups() default {
    }
    ;
    Class<? extends Payload>[] payload() default {
    }
    ;
}
