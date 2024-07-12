package aor.fpbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
/**
 * JacksonObjectMapperProvider is a context resolver for configuring Jackson's {@link ObjectMapper}.
 * <p>
 * This class provides a customized {@link ObjectMapper} to be used by JAX-RS for JSON serialization
 * and deserialization.
 * <br>
 */
@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public JacksonObjectMapperProvider() {
        mapper = ObjectMapperConfigurator.configureJackson();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}

