package aor.fpbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class UserUpdateDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private String username;

    @XmlElement
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
    private String firstName;

    @XmlElement
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    private String lastName;

    @XmlElement
    @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
    private String photo;

    @XmlElement
    private String biography;

    @XmlElement
    @Min(value = 1, message = "Laboratory ID must be greater than 0")
    private long laboratoryId;

    @XmlElement
    @JsonProperty("isPrivate")
    private boolean isPrivate;


    public UserUpdateDto() {
    }

    public UserUpdateDto(long id, String username, String firstName, String lastName, String photo, String biography, long laboratoryId, boolean isPrivate) {
        this.id = id;
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
