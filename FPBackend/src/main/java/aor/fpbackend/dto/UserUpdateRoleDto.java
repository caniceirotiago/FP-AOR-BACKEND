package aor.fpbackend.dto;

import aor.fpbackend.enums.UserRoleEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class UserUpdateRoleDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "Id must be greater than 0")
    private long userId;

    @XmlElement
    @NotNull
    @Enumerated
    private UserRoleEnum role;


    public UserUpdateRoleDto() {
    }

    public UserUpdateRoleDto(long userId, UserRoleEnum role) {
        this.userId = userId;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public UserRoleEnum getRole() {
        return role;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }
}
