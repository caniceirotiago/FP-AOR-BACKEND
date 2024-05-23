package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidRequestOnRegistConfirmationException extends Exception {
    public InvalidRequestOnRegistConfirmationException(String message) {
        super(message);
    }
}
