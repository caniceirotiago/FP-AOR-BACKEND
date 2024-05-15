package aor.fpbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperConfigurator {
    public static ObjectMapper configureJackson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // Support for Java 8 Date/Time API
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // To serialize dates as ISO strings
        return mapper;
    }
}
