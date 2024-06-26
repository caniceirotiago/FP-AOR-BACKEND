package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }

}
