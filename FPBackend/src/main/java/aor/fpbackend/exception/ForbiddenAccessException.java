package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ForbiddenAccessException extends Exception {
    public ForbiddenAccessException(String message) {
        super(message);
    }
}
