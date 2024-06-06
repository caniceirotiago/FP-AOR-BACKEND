package aor.fpbackend.dto;

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
    @Min(value = 1, message = "Project Id must be greater than 0")
    private long projectId;
    @XmlElement
    @NotNull
    @Min(value = 1, message = "User Id must be greater than 0")
    private long userId;
    @XmlElement
    @Enumerated
    @NotNull
    private ProjectRoleEnum role;

    public ProjectRoleUpdateDto() {
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public ProjectRoleEnum getRole() {
        return role;
    }

    public void setRole(ProjectRoleEnum role) {
        this.role = role;
    }
}
