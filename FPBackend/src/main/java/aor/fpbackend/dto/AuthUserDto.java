package aor.fpbackend.dto;
import aor.fpbackend.entity.MethodEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;
import java.util.Set;


@XmlRootElement
    public class AuthUserDto implements Serializable, Principal {

        @XmlElement
        @NotNull
        private Long userId;

        @XmlElement
        @NotNull
        private Long roleId;
        @XmlElement
        @NotNull
        private Set<MethodEntity> permissions;

        // Constructors
        public AuthUserDto() {}

        public AuthUserDto(Long userId, Long roleId, Set<MethodEntity> permissions) {
            this.userId = userId;
            this.roleId = roleId;
            this.permissions = permissions;
        }

    // Implementing the getName method from Principal

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }

    // Getters and setters


    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Set<MethodEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<MethodEntity> permissions) {
        this.permissions = permissions;
    }
}
