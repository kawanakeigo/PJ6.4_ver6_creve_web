package world.creve.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CodePointLengthValidator implements ConstraintValidator<CodePointLength, String> {
    private int min;
    private int max;
    private boolean strip;

    @Override
    public void initialize(CodePointLength constraint) {
        if (constraint.min() < 0 || constraint.max() < constraint.min()) {
            throw new IllegalArgumentException("Invalid code point length range");
        }
        min = constraint.min();
        max = constraint.max();
        strip = constraint.strip();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String candidate = strip ? value.strip() : value;
        int length = candidate.codePointCount(0, candidate.length());
        return min <= length && length <= max;
    }
}
