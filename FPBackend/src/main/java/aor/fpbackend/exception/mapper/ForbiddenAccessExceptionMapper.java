package aor.fpbackend.exception.mapper;
import aor.fpbackend.dto.Error.Error;
import aor.fpbackend.exception.ForbiddenAccessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class ForbiddenAccessExceptionMapper implements ExceptionMapper<ForbiddenAccessException> {
    private static final Logger LOGGER = LogManager.getLogger(ForbiddenAccessExceptionMapper.class);

    @Override
    public Response toResponse(ForbiddenAccessException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to access forbidden information: " + error.getMessage());
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
