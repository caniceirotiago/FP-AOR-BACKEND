package aor.fpbackend.exception.mapper;

import aor.fpbackend.dto.Error;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
private static final Logger LOGGER = LogManager.getLogger(IllegalStateException.class);

@Override
public Response toResponse(IllegalStateException e) {
    Error error = new Error(e.getMessage());
    LOGGER.warn("IllegalStateException: " + error.getMessage());
    return Response
            .status(Response.Status.CONFLICT)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
}
}