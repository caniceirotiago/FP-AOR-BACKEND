package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "project_log")

@NamedQuery(name = "ProjectLog.findAllProjectLogs", query = "SELECT p FROM ProjectLogEntity p")

public class ProjectLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "log_type", nullable = false)
    private String type;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    // Constructors
    public ProjectLogEntity() {}

    public ProjectLogEntity(Instant creationDate, String type, String content, UserEntity user, ProjectEntity project) {
        this.creationDate = creationDate;
        this.type = type;
        this.content = content;
        this.user = user;
        this.project = project;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    @Override
    public String toString() {
        return "ProjectLogEntity{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", project=" + project +
                '}';
    }
}
