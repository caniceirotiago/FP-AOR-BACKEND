package aor.fpbackend.exception.mapper;

import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import aor.fpbackend.dto.Error.Error;
import java.time.LocalDateTime;

@Provider
public class DatabaseOperationExceptionMapper implements ExceptionMapper<DatabaseOperationException> {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseOperationExceptionMapper.class);

    @Override
    public Response toResponse(DatabaseOperationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.error("Database operation failed " + LocalDateTime.now() + ": " + error.getMessage());
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
