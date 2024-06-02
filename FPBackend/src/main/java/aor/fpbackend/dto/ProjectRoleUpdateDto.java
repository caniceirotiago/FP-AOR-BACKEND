package aor.fpbackend.dto;

import aor.fpbackend.enums.ProjectRoleEnum;

public class ProjectRoleUpdateDto {
    private long projectId;
    private long userId;
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
