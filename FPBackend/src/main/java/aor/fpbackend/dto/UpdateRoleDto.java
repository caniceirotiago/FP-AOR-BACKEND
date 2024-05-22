package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class UpdateRoleDto implements Serializable {

    @XmlElement
    private long id;
    @XmlElement
    private String username;
    @XmlElement
    @NotNull
    private long roleId;

    public UpdateRoleDto() {
    }

    public UpdateRoleDto(long id, String username, long roleId) {
        this.id = id;
        this.username = username;
        this.roleId = roleId;
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

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
}
