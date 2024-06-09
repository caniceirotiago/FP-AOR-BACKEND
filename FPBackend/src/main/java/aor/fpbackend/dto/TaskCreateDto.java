package aor.fpbackend.dto;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class TaskCreateDto implements Serializable {

    @XmlElement
    @NotNull
    @Size(min = 2, max = 25, message = "Title must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Title must start with a letter")
    private String title;

    @XmlElement
    @NotNull
    @Size(min = 1, max = 2048, message = "Motivation must be between 1 and 2048 characters")
    private String description;

    @XmlElement
    private Instant plannedStartDate;

    @XmlElement
    private Instant plannedEndDate;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long responsibleId;

    public TaskCreateDto() {
    }

    public TaskCreateDto(String title, String description, Instant plannedStartDate, Instant plannedEndDate, long responsibleId) {
        this.title = title;
        this.description = description;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.responsibleId = responsibleId;
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
}