package aor.fpbackend.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import java.io.Serializable;

public class Error implements Serializable {

    @JsonbProperty("message")
    private final String message;


    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
