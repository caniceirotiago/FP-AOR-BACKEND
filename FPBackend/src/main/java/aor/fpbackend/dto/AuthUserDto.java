package aor.fpbackend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;


@XmlRootElement
    public class AuthUserDto implements Serializable, Principal {

        @XmlElement
        @NotNull
        private String username;

        @XmlElement
        @NotNull
        private long roleId;

        @XmlElement
        @NotNull
        @JsonProperty("sessionToken")
        private String sessionToken;

        // Constructors
        public AuthUserDto() {}

        public AuthUserDto(String username, long roleId, String sessionToken) {
            this.username = username;
            this.roleId = roleId;
            this.sessionToken = sessionToken;
        }

    // Implementing the getName method from Principal
    @Override
    public String getName() {
        return this.username;
    }
    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
