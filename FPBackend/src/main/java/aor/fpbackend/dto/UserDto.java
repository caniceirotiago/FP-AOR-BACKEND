package aor.fpbackend.dto;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;

@XmlRootElement
public class UserDto implements Serializable, Principal {

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
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    private String photo;

    @XmlElement
    @NotNull
    private String biography;

    @XmlElement
    @NotNull
    private long laboratoryId;

    @XmlElement
    @NotNull
    private long roleId;

    @XmlElement
    @NotNull
    private boolean isPrivate;

    @XmlElement
    @NotNull
    private boolean isDeleted;

    @XmlElement
    @NotNull
    private boolean isConfirmed;

    public UserDto() {
    }

    public UserDto(long id, String email, String password, String username, String firstName, String lastName, String photo, String biography, long laboratoryId, long roleId, boolean isPrivate, boolean isDeleted, boolean isConfirmed) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.biography = biography;
        this.laboratoryId = laboratoryId;
        this.roleId = roleId;
        this.isPrivate = isPrivate;
        this.isDeleted = isDeleted;
        this.isConfirmed = isConfirmed;
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

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
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

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photo='" + photo + '\'' +
                ", biography='" + biography + '\'' +
                ", laboratoryId=" + laboratoryId +
                ", roleId=" + roleId +
                ", isPrivate=" + isPrivate +
                ", isDeleted=" + isDeleted +
                ", isConfirmed=" + isConfirmed +
                '}';
    }
}
