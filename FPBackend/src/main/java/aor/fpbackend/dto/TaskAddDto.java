package aor.fpbackend.dto;

import aor.fpbackend.enums.AssetTypeEnum;
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
public class TaskAddDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Title must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Title must start with a letter")
    private String title;

    @XmlElement
    private String description;

    @XmlElement
    private Instant plannedStartDate;

    @XmlElement
    private Instant plannedEndDate;

    @XmlElement
    private long responsibleId;

    @XmlElement
    private Set<UsernameDto> addExecuters;

    @XmlElement
    private Set<Long> dependentTasks;

    @XmlElement
    private long projectId;

    public TaskAddDto() {
    }

    public TaskAddDto(String title, String description, Instant plannedStartDate, Instant plannedEndDate, long responsibleId,
                      Set<UsernameDto> addExecuters, Set<Long> dependentTasks, long projectId) {
        this.title = title;
        this.description = description;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.responsibleId = responsibleId;
        this.addExecuters = addExecuters;
        this.dependentTasks = dependentTasks;
        this.projectId = projectId;
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

    public long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(long responsibleId) {
        this.responsibleId = responsibleId;
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

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}