package aor.fpbackend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;



    @XmlRootElement
    public class EditProfileDto implements Serializable {

        @XmlElement
        String email;

        @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
        private String firstName;

        @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
        private String lastName;

        @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Invalid phone number")
        private String phoneNumber;

        @Size(min = 2, max = 2048, message = "Photo URL must be between 2 and 2048 characters")
        private String photoURL;
        private Boolean deleted;
        private String role;





}
