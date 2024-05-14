package aor.fpbackend.dto;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDto {

    @XmlElement
    @NotNull
    private long id;

    @XmlElement
    @NotNull
    @Email(message = "Email is required")
    private String email;

    @XmlElement
    @NotNull
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String password;

    @XmlElement
    @NotNull(message = "Nickname is required")
    @Size(min = 2, max = 25, message = "Nickname must be between 2 and 20 characters")
    private String nickname;

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
    @NotBlank
    private String photoURL;

    @XmlElement
    @NotBlank
    private String biography;

    @XmlElement
    @NotNull
    @NotBlank
    private String appRole;

    @XmlElement
    @NotNull
    private boolean isPrivate;

    @XmlElement
    @NotNull
    private boolean isDeleted;

    public UserDto() {
    }

    public UserDto(long id, String email, String password, String nickname, String firstName, String lastName, String photoURL, String biography, String appRole, boolean isPrivate, boolean isDeleted) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoURL = photoURL;
        this.biography = biography;
        this.appRole = appRole;
        this.isPrivate = isPrivate;
        this.isDeleted = isDeleted;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getAppRole() {
        return appRole;
    }

    public void setAppRole(String appRole) {
        this.appRole = appRole;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
