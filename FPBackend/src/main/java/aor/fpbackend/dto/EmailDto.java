package aor.fpbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

public class EmailDto implements Serializable {

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    public EmailDto() {
    }

    public EmailDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}