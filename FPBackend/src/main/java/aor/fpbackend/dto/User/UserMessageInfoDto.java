package aor.fpbackend.dto.User;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class UserMessageInfoDto implements Serializable {

    @XmlElement
    @NotNull
    private long id;

    @XmlElement
    @NotNull(message = "Username is required")
    @Size(min = 2, max = 25, message = "Username must be between 2 and 25 characters")
    private String username;

    @XmlElement
    @NotNull(message = "First name is required")
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    private String firstName;


    public UserMessageInfoDto() {
    }

    public UserMessageInfoDto(long id, String username, String firstName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
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

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
