package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.ProjectRoleEnum;

import java.io.Serializable;

@Entity
@Table(name = "project_membership")

@NamedQuery(name = "ProjectMembership.findProjectMembershipId", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.id = :id")
@NamedQuery(name = "ProjectMembership.findProjectMembershipsByProject", query = "SELECT new aor.fpbackend.dto.ProjectMembershipDto(" +
        "p.id, u.id, pr.id, p.role, p.isAccepted, new aor.fpbackend.dto.UserBasicInfoDto(u.id, u.username, u.photo, u.role.id)) " +
        "FROM ProjectMembershipEntity p JOIN p.user u JOIN p.project pr WHERE pr.id = :projectId")
@NamedQuery(name = "ProjectMembership.findProjectMembershipByAcceptanceToken", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.acceptanceToken = :acceptanceToken")
@NamedQuery(name = "ProjectMembership.findProjectMembershipByProjectIdAndUserId", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.project.id = :projectId AND p.user.id = :userId")
@NamedQuery(name = "ProjectMembership.findProjectManagers", query = "SELECT p.user FROM ProjectMembershipEntity p WHERE p.project.id = :projectId AND p.role = aor.fpbackend.enums.ProjectRoleEnum.PROJECT_MANAGER")
public class ProjectMembershipEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRoleEnum role;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted = false;

    @Column(name = "acceptance_token", nullable = true)
    private String acceptanceToken;

    // Constructors, getters, and setters

    public ProjectMembershipEntity() {}


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
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
