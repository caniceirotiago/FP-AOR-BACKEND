package aor.fpbackend.exception.mapper;
import aor.fpbackend.dto.Error;
import aor.fpbackend.exception.InvalidCredentialsException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

@Provider
public class InvalidCredentialsExceptionMapper implements ExceptionMapper<InvalidCredentialsException> {
    private static final Logger LOGGER = LogManager.getLogger(InvalidCredentialsExceptionMapper.class);

    @Override
    public Response toResponse(InvalidCredentialsException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to register with invalid credentials: " + error.getMessage());

        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
