package aor.fpbackend.dto.Password;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PasswordResetDto implements Serializable {

    @XmlElement
    @NotNull
    private String resetToken;
    @XmlElement
    @NotNull
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String newPassword;

    public PasswordResetDto() {
    }

    public PasswordResetDto(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
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
