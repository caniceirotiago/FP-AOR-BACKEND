package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = true)
    private String nickname;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "workplace", nullable = true)
    private String workplace;

    @Column(name = "photo", nullable = true, length = 2048)
    private String photo;

    @Column(name = "biography", nullable = true, length = 4096)
    private String biography;

    @Column(name = "role", nullable = true)
    private String role;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
    @OneToMany(mappedBy = "user")
    private Set<ProjectMembershipEntity> projects = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SessionEntity> sessions = new HashSet<>();
    @OneToMany(mappedBy = "responsibleUser")
    private Set<TaskEntity> responsibleTasks = new HashSet<>();

    @ManyToMany(mappedBy = "additionalExecuters")
    private Set<TaskEntity> tasksAsExecutor = new HashSet<>();

    public UserEntity() {}

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<ProjectMembershipEntity> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectMembershipEntity> projects) {
        this.projects = projects;
    }

    public Set<SessionEntity> getSessions() {
        return sessions;
    }

    public void setSessions(Set<SessionEntity> sessions) {
        this.sessions = sessions;
    }

    public Set<TaskEntity> getResponsibleTasks() {
        return responsibleTasks;
    }

    public void setResponsibleTasks(Set<TaskEntity> responsibleTasks) {
        this.responsibleTasks = responsibleTasks;
    }

    public Set<TaskEntity> getTasksAsExecutor() {
        return tasksAsExecutor;
    }

    public void setTasksAsExecutor(Set<TaskEntity> tasksAsExecutor) {
        this.tasksAsExecutor = tasksAsExecutor;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", firstName='" + firstName + '\'' +
                ", workplace='" + workplace + '\'' +
                ", photo='" + photo + '\'' +
                ", biography='" + biography + '\'' +
                ", role='" + role + '\'' +
                ", isPrivate=" + isPrivate +
                ", isDeleted=" + isDeleted +
                '}';
    }
}

