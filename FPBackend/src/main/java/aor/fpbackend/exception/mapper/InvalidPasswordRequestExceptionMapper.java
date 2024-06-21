package aor.fpbackend.exception.mapper;
import aor.fpbackend.dto.Error;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import aor.fpbackend.exception.InvalidPasswordRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class InvalidPasswordRequestExceptionMapper implements ExceptionMapper<InvalidPasswordRequestException> {
    private static final Logger LOGGER = LogManager.getLogger(InvalidPasswordRequestExceptionMapper.class);

    @Override
    public Response toResponse(InvalidPasswordRequestException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Not possible to change password: " + error.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
