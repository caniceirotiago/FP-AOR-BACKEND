package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.TaskStateEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")

@NamedQuery(name = "Task.findAll", query = "SELECT t FROM TaskEntity t")
@NamedQuery(name = "Task.findTaskByTitle", query = "SELECT t FROM TaskEntity t WHERE LOWER(t.title) = LOWER(:title)")
@NamedQuery(name = "Task.findTaskById", query = "SELECT t FROM TaskEntity t WHERE t.id = :taskId")
@NamedQuery(name = "Task.countTaskByTitle", query = "SELECT COUNT(t) FROM TaskEntity t WHERE LOWER(t.title) = LOWER(:title)")

public class TaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "title", nullable = false, updatable = false)
    private String title;

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "planned_start_date", nullable = true)
    private Instant plannedStartDate;

    @Column(name = "start_date", nullable = true)
    private Instant startDate;

    @Column(name = "planned_end_date", nullable = true)
    private Instant plannedEndDate;

    @Column(name = "end_date", nullable = true)
    private Instant endDate;

    @Column(name = "duration", nullable = true)
    private long duration; // Counting in days

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TaskStateEnum state;


    // Relationships
    @ManyToOne
    @JoinColumn(name = "responsible_user_id")
    private UserEntity responsibleUser;

    @ManyToMany
    @JoinTable(
            name = "task_registered_executors",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> registeredExecutors = new HashSet<>();

    @Column(name = "additional_executors", nullable = true)
    private String additionalExecutors;

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

    // Constructors, getters, and setters

    public TaskEntity() {}

    public TaskEntity(String title, String description, Instant creationDate, long duration, TaskStateEnum state, ProjectEntity project, UserEntity responsibleUser) {
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.duration = duration;
        this.state = state;
        this.project = project;
        this.responsibleUser = responsibleUser;
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

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(Instant plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TaskStateEnum getState() {
        return state;
    }

    public void setState(TaskStateEnum state) {
        this.state = state;
    }

    public UserEntity getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(UserEntity responsibleUser) {
        this.responsibleUser = responsibleUser;
    }

    public Set<UserEntity> getRegisteredExecutors() {
        return registeredExecutors;
    }

    public void setRegisteredExecutors(Set<UserEntity> registeredExecutors) {
        this.registeredExecutors = registeredExecutors;
    }

    public String getAdditionalExecutors() {
        return additionalExecutors;
    }

    public void setAdditionalExecutors(String additionalExecutors) {
        this.additionalExecutors = additionalExecutors;
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

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", plannedStartDate=" + plannedStartDate +
                ", startDate=" + startDate +
                ", plannedEndDate=" + plannedEndDate +
                ", endDate=" + endDate +
                ", duration=" + duration +
                ", state=" + state +
                ", responsibleUser=" + responsibleUser +
                ", registeredExecutors=" + registeredExecutors +
                ", additionalExecutors='" + additionalExecutors + '\'' +
                ", project=" + project +
                ", prerequisites=" + prerequisites +
                ", dependentTasks=" + dependentTasks +
                '}';
    }
}
