package aor.fpbackend.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * GsonSetup provides a customized Gson instance with adapters for Java 8 date and time types.
 * <p>
 * This utility class creates a Gson instance that is configured to handle serialization and deserialization
 * of {@link LocalDateTime}, {@link Instant}, and {@link LocalDate} types.
 * </p>
 */
public class GsonSetup {
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }
}

