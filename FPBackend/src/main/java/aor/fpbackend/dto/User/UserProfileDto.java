package aor.fpbackend.dto.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class UserProfileDto implements Serializable {

    @XmlElement
    @NotNull
    private long id;

    @XmlElement
    @NotNull
    @Email(message = "Invalid email format")
    private String email;

    @XmlElement
    @NotNull
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
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    private String photo;

    @XmlElement
    @NotNull
    private String biography;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "Laboratory ID must be greater than 0")
    private long laboratoryId;

    @XmlElement
    @NotNull
    private boolean isPrivate;

    public UserProfileDto() {
    }

    public UserProfileDto(long id, String email, String username, String firstName, String lastName, String photo, String biography, long laboratoryId, boolean isPrivate) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.biography = biography;
        this.laboratoryId = laboratoryId;
        this.isPrivate = isPrivate;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public long getLaboratoryId() {
        return laboratoryId;
    }

    public void setLaboratoryId(long laboratoryId) {
        this.laboratoryId = laboratoryId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
