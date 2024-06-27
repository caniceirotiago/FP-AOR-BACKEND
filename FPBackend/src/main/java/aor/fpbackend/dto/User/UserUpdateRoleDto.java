package aor.fpbackend.dto.User;

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
    @Min(value = 1, message = "Role Id must be greater than 0")
    private long roleId;


    public UserUpdateRoleDto() {
    }

    public UserUpdateRoleDto(long userId, long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
}
