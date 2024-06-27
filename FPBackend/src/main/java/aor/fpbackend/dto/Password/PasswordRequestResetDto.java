package aor.fpbackend.dto.Password;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class PasswordRequestResetDto implements Serializable {

    @XmlElement
    @NotNull
    private String email;

    public PasswordRequestResetDto() {
    }

    public PasswordRequestResetDto(String email) {
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

