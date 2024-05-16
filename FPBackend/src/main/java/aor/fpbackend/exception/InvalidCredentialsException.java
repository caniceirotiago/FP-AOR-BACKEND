package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidCredentialsException extends Exception{
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
