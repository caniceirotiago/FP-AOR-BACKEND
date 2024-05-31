package aor.fpbackend.dto;

import aor.fpbackend.enums.ProjectRoleEnum;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class ProjectMembershipDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private long userId;

    @XmlElement
    private long projectId;

    @XmlElement
    private ProjectRoleEnum role;

    @XmlElement
    private boolean isAccepted;

    @XmlElement
    private String acceptanceToken;

    public ProjectMembershipDto() {}

    public ProjectMembershipDto(long id, long userId, long projectId, ProjectRoleEnum role, boolean isAccepted, String acceptanceToken) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.role = role;
        this.isAccepted = isAccepted;
        this.acceptanceToken = acceptanceToken;
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public ProjectRoleEnum getRole() {
        return role;
    }

    public void setRole(ProjectRoleEnum role) {
        this.role = role;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public String getAcceptanceToken() {
        return acceptanceToken;
    }

    public void setAcceptanceToken(String acceptanceToken) {
        this.acceptanceToken = acceptanceToken;
    }
}
