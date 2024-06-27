package aor.fpbackend.exception.mapper;

import aor.fpbackend.dto.Error.Error;
import aor.fpbackend.exception.EntityNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
    private static final Logger LOGGER = LogManager.getLogger(EntityNotFoundExceptionMapper.class);

    @Override
    public Response toResponse(EntityNotFoundException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Entity not found: " + error.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
