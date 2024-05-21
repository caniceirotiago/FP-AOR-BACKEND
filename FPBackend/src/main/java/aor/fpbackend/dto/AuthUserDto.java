package aor.fpbackend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.security.Principal;


@XmlRootElement
    public class AuthUserDto implements Serializable, Principal {

        @XmlElement
        @NotNull
        private String username;

        @XmlElement
        @NotNull
        private String role;

        @XmlElement
        @NotNull
        @JsonProperty("sessionToken")
        private String sessionToken;

        // Constructors
        public AuthUserDto() {}

        public AuthUserDto(String username, String role, String sessionToken) {
            this.username = username;
            this.role = role;
            this.sessionToken = sessionToken;
        }

    // Implementing the getName method from Principal
    @Override
    public String getName() {
        return this.username;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
