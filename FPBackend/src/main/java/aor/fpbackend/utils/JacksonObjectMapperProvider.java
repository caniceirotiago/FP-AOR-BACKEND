package aor.fpbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

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

