package aor.fpbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
/**
 * ObjectMapperConfigurator provides a method to configure a Jackson {@link ObjectMapper}
 * with support for Java 8 Date/Time API and to serialize dates as ISO strings.
 */
public class ObjectMapperConfigurator {
    public static ObjectMapper configureJackson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // Support for Java 8 Date/Time API
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // To serialize dates as ISO strings
        return mapper;
    }
}
