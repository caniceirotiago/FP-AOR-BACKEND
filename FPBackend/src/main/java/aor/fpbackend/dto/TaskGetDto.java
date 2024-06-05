package aor.fpbackend.dto;

import aor.fpbackend.enums.TaskStateEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class TaskGetDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Title must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Title must start with a letter")
    private String title;

    @XmlElement
    private String description;

    @XmlElement
    private Instant creationDate;

    @XmlElement
    private Instant plannedStartDate;

    @XmlElement
    private Instant startDate;

    @XmlElement
    private Instant plannedEndDate;

    @XmlElement
    private Instant endDate;

    @XmlElement
    private long duration;

    @XmlElement
    @Enumerated
    private TaskStateEnum state;

    @XmlElement
    private long responsibleId;

    @XmlElement
    private Set<UserBasicInfoDto> registeredExecutors;

    @XmlElement
    private String nonRegisteredExecutors;

    @XmlElement
    private Set<Long> dependentTasks;

    @XmlElement
    private long projectId;

    public TaskGetDto() {
    }

    public TaskGetDto(long id, String title, String description, Instant creationDate, Instant plannedStartDate,
                      Instant startDate, Instant plannedEndDate, Instant endDate, long duration, TaskStateEnum state, long responsibleId,
                      Set<UserBasicInfoDto> registeredExecutors, String nonRegisteredExecutors, Set<Long> dependentTasks, long projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.plannedStartDate = plannedStartDate;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.endDate = endDate;
        this.duration = duration;
        this.state = state;
        this.responsibleId = responsibleId;
        this.registeredExecutors = registeredExecutors;
        this.nonRegisteredExecutors = nonRegisteredExecutors;
        this.dependentTasks = dependentTasks;
        this.projectId = projectId;
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

    public long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(long responsibleId) {
        this.responsibleId = responsibleId;
    }

    public Set<UserBasicInfoDto> getRegisteredExecutors() {
        return registeredExecutors;
    }

    public void setRegisteredExecutors(Set<UserBasicInfoDto> registeredExecutors) {
        this.registeredExecutors = registeredExecutors;
    }

    public String getNonRegisteredExecutors() {
        return nonRegisteredExecutors;
    }

    public void setNonRegisteredExecutors(String nonRegisteredExecutors) {
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }

    public Set<Long> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(Set<Long> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}