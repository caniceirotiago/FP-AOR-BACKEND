package aor.fpbackend.dto;

import aor.fpbackend.enums.TaskStateEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@XmlRootElement
public class TaskDetailedUpdateDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long taskId;

    @XmlElement
    @NotNull
    private String title;

    @XmlElement
    @NotNull
    private String description;

    @XmlElement
    private Instant plannedStartDate;

    @XmlElement
    private Instant plannedEndDate;

    @XmlElement
    @Enumerated
    @NotNull
    private TaskStateEnum state;

    @XmlElement
    private long responsibleUserId;

    @XmlElement
    private List<Long> registeredExecutors;

    @XmlElement
    private String nonRegisteredExecutors;



    public TaskDetailedUpdateDto() {
    }

    public TaskDetailedUpdateDto(long taskId, String title, String description, Instant plannedStartDate, Instant plannedEndDate, TaskStateEnum state, long responsibleUserId, List<Long> registeredExecutors, String nonRegisteredExecutors) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.state = state;
        this.responsibleUserId = responsibleUserId;
        this.registeredExecutors = registeredExecutors;
        this.nonRegisteredExecutors = nonRegisteredExecutors;

    }

    // Getters and Setters

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

    public TaskStateEnum getState() {
        return state;
    }

    public void setState(TaskStateEnum state) {
        this.state = state;
    }

    public long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public List<Long> getRegisteredExecutors() {
        return registeredExecutors;
    }

    public void setRegisteredExecutors(List<Long> registeredExecutors) {
        this.registeredExecutors = registeredExecutors;
    }

    public String getNonRegisteredExecutors() {
        return nonRegisteredExecutors;
    }

    public void setNonRegisteredExecutors(String nonRegisteredExecutors) {
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }


}