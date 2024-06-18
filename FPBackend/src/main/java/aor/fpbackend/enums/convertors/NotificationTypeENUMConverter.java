package aor.fpbackend.enums.convertors;

import aor.fpbackend.enums.NotificationTypeENUM;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationTypeENUMConverter implements AttributeConverter<NotificationTypeENUM, Integer> {

    @Override
    public Integer convertToDatabaseColumn(NotificationTypeENUM attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getIntValue();
    }

    @Override
    public NotificationTypeENUM convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return NotificationTypeENUM.fromInt(dbData);
    }
}
