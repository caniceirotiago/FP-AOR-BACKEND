package aor.fpbackend.dto;

import aor.fpbackend.enums.TaskStateEnum;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class TaskGetDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
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
    private UserBasicInfoDto responsibleId;

    @XmlElement
    private Set<UserBasicInfoDto> registeredExecutors;

    @XmlElement
    private String nonRegisteredExecutors;

    @XmlElement
    private Set<Long> dependentTasks;

    @XmlElement
    private Set<Long> prerequisites;

    @XmlElement
    private long projectId;

    @XmlElement
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    public TaskGetDto() {
    }

    public TaskGetDto(long id, String title, String description, Instant creationDate, Instant plannedStartDate,
                      Instant startDate, Instant plannedEndDate, Instant endDate, long duration, TaskStateEnum state, UserBasicInfoDto responsibleId,
                      Set<UserBasicInfoDto> registeredExecutors, String nonRegisteredExecutors, Set<Long> dependentTasks, Set<Long> prerequisites, long projectId, Boolean isDeleted) {
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
        this.prerequisites = prerequisites;
        this.projectId = projectId;
        this.isDeleted = isDeleted;
    }

    // getters e setters


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

    public UserBasicInfoDto getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(UserBasicInfoDto responsibleId) {
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

    public Set<Long> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(Set<Long> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return "TaskGetDto{" +
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
                ", responsibleId=" + responsibleId +
                ", registeredExecutors=" + registeredExecutors +
                ", nonRegisteredExecutors='" + nonRegisteredExecutors + '\'' +
                ", dependentTasks=" + dependentTasks +
                ", prerequisites=" + prerequisites +
                ", projectId=" + projectId +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
