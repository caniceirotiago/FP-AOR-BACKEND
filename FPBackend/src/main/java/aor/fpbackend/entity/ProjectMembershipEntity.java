package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.ProjectRoleType;

import java.io.Serializable;

@Entity
@Table(name = "project_membership")
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
    private ProjectRoleType role;

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

    public ProjectRoleType getRole() {
        return role;
    }

    public void setRole(ProjectRoleType role) {
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
