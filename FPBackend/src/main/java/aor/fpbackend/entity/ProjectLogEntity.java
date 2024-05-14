package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "project_log")
public class ProjectLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskEntity task;  // This can be null if the log is not directly related to a specific task.

    // Constructors
    public ProjectLogEntity() {}

    public ProjectLogEntity(Instant creationDate, String type, String content, UserEntity user, ProjectEntity project, TaskEntity task) {
        this.creationDate = creationDate;
        this.type = type;
        this.content = content;
        this.user = user;
        this.project = project;
        this.task = task;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }
}
