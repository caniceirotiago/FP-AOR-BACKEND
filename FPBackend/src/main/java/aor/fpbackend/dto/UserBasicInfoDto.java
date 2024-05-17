package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

@XmlRootElement
public class UserBasicInfoDto implements Serializable {

    @XmlElement
    @NotNull
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    @NotBlank
    private String photo;

    @XmlElement
    @NotNull(message = "Nickname is required")
    @Size(min = 2, max = 25, message = "Nickname must be between 2 and 25 characters")
    private String nickname;

    @XmlElement
    @NotNull(message = "Role is required")
    private String role;

    public UserBasicInfoDto() {
    }

    public UserBasicInfoDto(String photo, String nickname, String role) {
        this.photo = photo;
        this.nickname = nickname;
        this.role = role;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
