package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class AttributeAlreadyExistsException extends Exception {
    public AttributeAlreadyExistsException(String message) {
        super(message);
    }
}