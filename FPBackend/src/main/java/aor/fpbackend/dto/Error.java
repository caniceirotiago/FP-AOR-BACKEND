package aor.fpbackend.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import java.io.Serializable;

public class Error implements Serializable {

    @JsonbProperty("errorMessage")
    private final String errorMessage;

    public Error(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
