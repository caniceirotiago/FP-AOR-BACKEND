package aor.fpbackend.exception.mapper;

import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import aor.fpbackend.dto.Error;

@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {
    private static final Logger LOGGER = LogManager.getLogger(UserNotFoundExceptionMapper.class);

    @Override
    public Response toResponse(UserNotFoundException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("User not found: " + error.getErrorMessage());
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
