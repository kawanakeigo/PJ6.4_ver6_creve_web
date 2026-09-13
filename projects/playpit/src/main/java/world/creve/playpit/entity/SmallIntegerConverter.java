package world.creve.playpit.entity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter public class SmallIntegerConverter implements AttributeConverter<Integer,Short> {
    public Short convertToDatabaseColumn(Integer value) {
        if(value==null)return null;
        if(value<1||value>5)throw new IllegalArgumentException("Invalid petal type");
        return value.shortValue();
    }
    public Integer convertToEntityAttribute(Short value) {
        return value==null?null:value.intValue();
    }
}
