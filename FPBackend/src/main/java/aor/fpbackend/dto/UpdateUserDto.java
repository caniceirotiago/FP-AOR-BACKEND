package aor.fpbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;

@XmlRootElement
public class UpdateUserDto implements Serializable, Principal {

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
    private String photo;

    @XmlElement
    @NotNull
    private String biography;

    @XmlElement
    @NotNull
    private long laboratoryId;

    @XmlElement
    @JsonProperty("isPrivate")
    @NotNull
    private boolean isPrivate;

    @XmlElement
    @JsonProperty("isDeleted")
    @NotNull
    private boolean isDeleted;

    public UpdateUserDto() {
    }

    public UpdateUserDto(String firstName, String lastName, String photo, String biography, long laboratoryId, boolean isPrivate, boolean isDeleted) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.biography = biography;
        this.laboratoryId = laboratoryId;
        this.isPrivate = isPrivate;
        this.isDeleted = isDeleted;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String toString() {
        return "UpdateUserDto{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photo='" + photo + '\'' +
                ", biography='" + biography + '\'' +
                ", laboratoryId=" + laboratoryId +
                ", isPrivate=" + isPrivate +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
