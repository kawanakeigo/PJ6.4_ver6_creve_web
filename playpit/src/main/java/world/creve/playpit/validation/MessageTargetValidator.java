package world.creve.playpit.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import world.creve.playpit.dto.request.MessageRequest;
public class MessageTargetValidator implements ConstraintValidator<ValidMessageTarget,MessageRequest> {
    public boolean isValid(MessageRequest value,ConstraintValidatorContext context) {
        return value!=null&&(value.getCreatorId()!=null||value.getArtworkId()!=null);
    }
}
