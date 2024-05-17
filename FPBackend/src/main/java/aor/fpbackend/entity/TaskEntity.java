package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.TaskState;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")
public class TaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "planned_start_date", nullable = true)
    private Instant plannedStartDate;

    @Column(name = "planned_end_date", nullable = true)
    private Instant plannedEndDate;

    @Column(name = "conclusion_date", nullable = true)
    private Instant conclusionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TaskState state;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private UserEntity responsibleUser;

    @ManyToMany
    @JoinTable(
            name = "task_executors",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> additionalExecuters = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToMany
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_task_id")
    )
    private Set<TaskEntity> prerequisites = new HashSet<>();

    @ManyToMany(mappedBy = "prerequisites")
    private Set<TaskEntity> dependentTasks = new HashSet<>();
    @OneToMany(mappedBy = "task")
    private Set<ProjectLogEntity> taskLogs = new HashSet<>();

    // Constructors, getters, and setters

    public TaskEntity() {}

    public TaskEntity(String title, String description, Instant creationDate, Instant plannedStartDate, Instant plannedEndDate) {
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(Instant plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public Instant getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(Instant plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public Instant getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(Instant conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public UserEntity getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(UserEntity responsibleUser) {
        this.responsibleUser = responsibleUser;
    }

    public Set<UserEntity> getAdditionalExecuters() {
        return additionalExecuters;
    }

    public void setAdditionalExecuters(Set<UserEntity> additionalExecuters) {
        this.additionalExecuters = additionalExecuters;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public Set<TaskEntity> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(Set<TaskEntity> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public Set<TaskEntity> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(Set<TaskEntity> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }

    public Set<ProjectLogEntity> getTaskLogs() {
        return taskLogs;
    }

    public void setTaskLogs(Set<ProjectLogEntity> taskLogs) {
        this.taskLogs = taskLogs;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", plannedStartDate=" + plannedStartDate +
                ", plannedEndDate=" + plannedEndDate +
                ", conclusionDate=" + conclusionDate +
                ", state='" + state + '\'' +
                ", responsibleUser=" + responsibleUser +
                ", additionalExecuters=" + additionalExecuters +
                ", project=" + project +
                '}';
    }
}
