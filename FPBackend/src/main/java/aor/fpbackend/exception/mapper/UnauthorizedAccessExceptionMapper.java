package aor.fpbackend.exception.mapper;
import aor.fpbackend.dto.Error;
import aor.fpbackend.exception.UnauthorizedAccessException;
import aor.fpbackend.exception.UserConfirmationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class UnauthorizedAccessExceptionMapper implements ExceptionMapper<UnauthorizedAccessException> {
    private static final Logger LOGGER = LogManager.getLogger(UnauthorizedAccessExceptionMapper.class);

    @Override
    public Response toResponse(UnauthorizedAccessException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to access unauthorized information: " + error.getErrorMessage());
        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
