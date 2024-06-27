package aor.fpbackend.dto.User;

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
    private long id;

    @XmlElement
    @NotNull(message = "Username is required")
    @Size(min = 2, max = 25, message = "Username must be between 2 and 25 characters")
    private String username;

    @XmlElement
    @NotNull
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    private String photo;

    @XmlElement
    @NotNull(message = "Role is required")
    private long role;

    public UserBasicInfoDto() {
    }

    public UserBasicInfoDto(long id, String username, String photo, long role) {
        this.id = id;
        this.username = username;
        this.photo = photo;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public long getRole() {
        return role;
    }

    public void setRole(long role) {
        this.role = role;
    }
}
