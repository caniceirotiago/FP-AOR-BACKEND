package aor.fpbackend.dto.Email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class EmailDto implements Serializable {

    @XmlElement
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