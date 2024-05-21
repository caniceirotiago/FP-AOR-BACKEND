package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class RequestResetPasswordDto implements Serializable {

    @XmlElement
    @NotNull
    private String email;

    public RequestResetPasswordDto() {
    }

    public RequestResetPasswordDto(String email) {
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

