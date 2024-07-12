package aor.fpbackend.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 * LocalDateAdapter is a custom serializer and deserializer for {@link LocalDate} objects
 * to and from JSON using the Gson library.
 * <p>
 * This adapter ensures that {@link LocalDate} objects are serialized to and deserialized
 * from the ISO-8601 date format.
 * </p>
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src)); // Serialize to ISO-8601 date format
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return LocalDate.parse(json.getAsString(), formatter); // Deserialize from ISO-8601 date format
    }
}

