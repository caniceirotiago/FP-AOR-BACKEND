package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

@XmlRootElement
public class UserBasicInfoDto implements Serializable {

//    @XmlElement
//    private long id;
    //TODO Boa prática passar id?

    @XmlElement
    @NotNull
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    @NotBlank
    private String photo;

    @XmlElement
    @NotNull(message = "Username is required")
    @Size(min = 2, max = 25, message = "Username must be between 2 and 25 characters")
    private String username;

    @XmlElement
    @NotNull(message = "Role is required")
    private long role;

    public UserBasicInfoDto() {
    }

    public UserBasicInfoDto(String photo, String username, long role) {
        this.photo = photo;
        this.username = username;
        this.role = role;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getRole() {
        return role;
    }

    public void setRole(long role) {
        this.role = role;
    }
}
