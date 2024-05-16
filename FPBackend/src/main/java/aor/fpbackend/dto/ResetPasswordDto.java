package aor.fpbackend.dto;

import java.io.Serializable;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResetPasswordDto implements Serializable {

    @XmlElement
    private String email;
    @XmlElement
    private String resetToken;
    @XmlElement
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String newPassword;

    public ResetPasswordDto() {
    }

    public ResetPasswordDto(String email, String resetToken, String newPassword) {
        this.email = email;
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String token) {
        this.resetToken = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
