package aor.fpbackend.exception.mapper;

import aor.fpbackend.dto.Error.Error;
import aor.fpbackend.exception.ElementAssociationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class ElementAssociationExceptionMapper implements ExceptionMapper<ElementAssociationException> {
    private static final Logger LOGGER = LogManager.getLogger(ElementAssociationException.class);

    @Override
    public Response toResponse(ElementAssociationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Association error: " + error.getMessage());
        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}