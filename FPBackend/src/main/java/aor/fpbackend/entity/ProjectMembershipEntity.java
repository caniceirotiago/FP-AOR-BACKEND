package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.ProjectRoleEnum;

import java.io.Serializable;

@Entity
@Table(name = "project_membership")

@NamedQuery(
        name = "ProjectMembership.findUsersByFirstLetterAndProjId",
        query = "SELECT u FROM ProjectMembershipEntity p JOIN p.user u WHERE p.project.id = :projectId AND u.username LIKE :firstLetter || '%' AND u.isConfirmed = true")
@NamedQuery(name = "ProjectMembership.findProjectIdsByUserId", query = "SELECT p.project.id FROM ProjectMembershipEntity p WHERE p.user.id = :userId")
@NamedQuery(name = "ProjectMembership.findProjectMembershipsByProject", query = "SELECT new aor.fpbackend.dto.Project.ProjectMembershipDto(" +
        "p.id, u.id, pr.id, p.role, p.isAccepted, new aor.fpbackend.dto.User.UserBasicInfoDto(u.id, u.username, u.photo, u.role.id)) " +
        "FROM ProjectMembershipEntity p JOIN p.user u JOIN p.project pr WHERE pr.id = :projectId")
@NamedQuery(name = "ProjectMembership.findProjectMembershipByAcceptanceToken", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.acceptanceToken = :acceptanceToken")
@NamedQuery(name = "ProjectMembership.findProjectMembershipByProjectIdAndUserId", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.project.id = :projectId AND p.user.id = :userId")
@NamedQuery(name = "ProjectMembership.isUserProjectMember", query = "SELECT p FROM ProjectMembershipEntity p WHERE p.project.id = :projectId AND p.user.id = :userId AND p.isAccepted = true")
@NamedQuery(name = "ProjectMembership.findProjectManagers", query = "SELECT p.user FROM ProjectMembershipEntity p WHERE p.project.id = :projectId AND p.role = aor.fpbackend.enums.ProjectRoleEnum.PROJECT_MANAGER")
@NamedQuery(name = "ProjectMembership.findProjectMembershipByProjectIdAndUserIdAndRole",
        query = "SELECT pm FROM ProjectMembershipEntity pm WHERE pm.project.id = :projectId AND pm.user.id = :userId AND pm.role = :role")
@NamedQuery(name = "ProjectMembership.findProjectActiveMembersByProjectId",
        query = "SELECT pm.user FROM ProjectMembershipEntity pm WHERE pm.project.id = :projectId AND pm.isAccepted = true")

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
    private boolean isAccepted;

    @Column(name = "acceptance_token", nullable = true)
    private String acceptanceToken;

    // Constructors, getters, and setters
    public ProjectMembershipEntity() {}

    public ProjectMembershipEntity(UserEntity user, ProjectEntity project, ProjectRoleEnum role, boolean isAccepted, String acceptanceToken) {
        this.user = user;
        this.project = project;
        this.role = role;
        this.isAccepted = isAccepted;
        this.acceptanceToken = acceptanceToken;
    }

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
