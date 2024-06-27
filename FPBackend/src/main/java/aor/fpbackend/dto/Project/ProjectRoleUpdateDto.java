package aor.fpbackend.dto.Project;

import aor.fpbackend.enums.ProjectRoleEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class ProjectRoleUpdateDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "User Id must be greater than 0")
    private long userId;
    @XmlElement
    @Enumerated
    @NotNull
    private ProjectRoleEnum newRole;

    public ProjectRoleUpdateDto() {
    }

    public ProjectRoleUpdateDto(long userId, ProjectRoleEnum newRole) {
        this.userId = userId;
        this.newRole = newRole;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public ProjectRoleEnum getNewRole() {
        return newRole;
    }

    public void setNewRole(ProjectRoleEnum newRole) {
        this.newRole = newRole;
    }
}
