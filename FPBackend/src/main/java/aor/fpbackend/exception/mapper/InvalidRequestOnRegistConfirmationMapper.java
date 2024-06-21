package aor.fpbackend.exception.mapper;

import aor.fpbackend.exception.InvalidRequestOnRegistConfirmationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class InvalidRequestOnRegistConfirmationMapper implements ExceptionMapper<InvalidRequestOnRegistConfirmationException> {
    private static final Logger LOGGER = LogManager.getLogger(InvalidRequestOnRegistConfirmationMapper.class);

    @Override
    public Response toResponse(InvalidRequestOnRegistConfirmationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt confirm registration with invalid confirmation request: " + error.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}

