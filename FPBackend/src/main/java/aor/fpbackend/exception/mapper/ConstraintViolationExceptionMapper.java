package aor.fpbackend.exception.mapper;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;

import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        final String errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        LOGGER.warn("ConstraintViolationException: {}", errors);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new Error(errors))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
