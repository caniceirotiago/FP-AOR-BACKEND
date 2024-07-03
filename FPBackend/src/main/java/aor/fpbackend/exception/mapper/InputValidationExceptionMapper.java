package aor.fpbackend.exception.mapper;

import aor.fpbackend.dto.Error.Error;
import aor.fpbackend.exception.InputValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class InputValidationExceptionMapper implements ExceptionMapper<InputValidationException> {
    private static final Logger LOGGER = LogManager.getLogger(InputValidationExceptionMapper.class);

    @Override
    public Response toResponse(InputValidationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Invalid inputs : " + error.getMessage());

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}