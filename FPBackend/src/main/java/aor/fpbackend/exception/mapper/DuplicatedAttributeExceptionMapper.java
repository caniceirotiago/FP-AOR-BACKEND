package aor.fpbackend.exception.mapper;

import aor.fpbackend.dto.Error;
import aor.fpbackend.exception.DuplicatedAttributeException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class DuplicatedAttributeExceptionMapper implements ExceptionMapper<DuplicatedAttributeException> {
    private static final Logger LOGGER = LogManager.getLogger(DuplicatedAttributeException.class);

    @Override
    public Response toResponse(DuplicatedAttributeException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attribute Already Exists: " + error.getErrorMessage());
        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}