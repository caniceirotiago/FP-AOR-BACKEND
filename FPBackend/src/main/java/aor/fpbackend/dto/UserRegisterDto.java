package aor.fpbackend.dto;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;

@XmlRootElement
public class UserRegisterDto implements Serializable {

    @XmlElement
    @NotNull
    @Email(message = "Invalid email format")
    private String email;

    @XmlElement
    @NotNull
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.])[A-Za-z\\d@$!%*?&#.]+$",
            message = "Password must include an uppercase letter, a lowercase letter, a digit, and one special character")
    private String password;

    @XmlElement
    @NotNull(message = "Username is required")
    @Size(min = 2, max = 25, message = "Username must be between 2 and 20 characters")
    private String username;

    @XmlElement
    @NotNull
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
    private String firstName;

    @XmlElement
    @NotNull
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    private String lastName;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "Laboratory ID must be greater than 0")
    private long laboratoryId;

    public UserRegisterDto() {
    }

    public UserRegisterDto(String email, String password, String username, String firstName, String lastName, long laboratoryId) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.laboratoryId = laboratoryId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getLaboratoryId() {
        return laboratoryId;
    }

    public void setLaboratoryId(long laboratoryId) {
        this.laboratoryId = laboratoryId;
    }
}
