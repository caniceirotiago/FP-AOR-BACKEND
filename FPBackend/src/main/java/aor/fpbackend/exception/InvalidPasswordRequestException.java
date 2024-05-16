package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidPasswordRequestException extends Exception {
    public InvalidPasswordRequestException(String message) {
        super(message);
    }
}
