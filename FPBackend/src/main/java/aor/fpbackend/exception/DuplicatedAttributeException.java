package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class DuplicatedAttributeException extends Exception {
    public DuplicatedAttributeException(String message) {
        super(message);
    }
}