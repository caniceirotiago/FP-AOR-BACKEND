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
public class TaskUpdateDto implements Serializable {

    @XmlElement
    private String description;

    @XmlElement
    private Instant plannedStartDate;

    @XmlElement
    private Instant plannedEndDate;

    @XmlElement
    @Enumerated
    private TaskStateEnum state;

    @XmlElement
    private Set<UsernameDto> addExecuters;

    @XmlElement
    private Set<Long> dependentTasks;

    public TaskUpdateDto() {
    }

    public TaskUpdateDto(String description, Instant plannedStartDate, Instant plannedEndDate, TaskStateEnum state, long responsibleId,
                         Set<UsernameDto> addExecuters, Set<Long> dependentTasks) {
        this.description = description;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.state = state;
        this.addExecuters = addExecuters;
        this.dependentTasks = dependentTasks;
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

    public Set<UsernameDto> getAddExecuters() {
        return addExecuters;
    }

    public void setAddExecuters(Set<UsernameDto> addExecuters) {
        this.addExecuters = addExecuters;
    }

    public Set<Long> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(Set<Long> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }
}