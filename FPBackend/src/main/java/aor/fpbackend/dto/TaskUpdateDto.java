package aor.fpbackend.dto;

import aor.fpbackend.enums.TaskStateEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class TaskUpdateDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long taskId;

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

    public TaskUpdateDto() {
    }

    public TaskUpdateDto(long taskId, String description, Instant plannedStartDate, Instant plannedEndDate, TaskStateEnum state) {
        this.taskId = taskId;
        this.description = description;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.state = state;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

}