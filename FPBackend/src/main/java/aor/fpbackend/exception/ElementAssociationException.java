package aor.fpbackend.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class ElementAssociationException extends Exception {
    public ElementAssociationException(String message) {
        super(message);
    }
}