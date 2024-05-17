package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class DatabaseOperationException extends Exception {
    public DatabaseOperationException(String message) {
        super(message);
    }
}
