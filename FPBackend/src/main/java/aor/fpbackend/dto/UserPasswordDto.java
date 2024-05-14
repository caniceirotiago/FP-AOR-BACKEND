package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserPasswordDto {


    @XmlElement
    private String password;
    @XmlElement
    @NotNull
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String newPassword;

    public UserPasswordDto() {
    }

    public UserPasswordDto(String password, String newPassword) {
        this.password = password;
        this.newPassword = newPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
