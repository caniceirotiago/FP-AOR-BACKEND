package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InputValidationException extends Exception {
    public InputValidationException(String message) {
        super(message);
    }
}
