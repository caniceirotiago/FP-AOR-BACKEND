package aor.fpbackend.dto;

import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class ProjectUpdateDto implements Serializable {

    @XmlElement
    @NotNull(message = "Project Id cannot be null")
    @Min(value = 1, message = "Id must be greater than 0")
    private Long id;

    @XmlElement
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;

    @XmlElement
    @Size(min = 1, max = 2048, message = "Description must be between 1 and 2048 characters")
    private String description;

    @XmlElement
    @Size(min = 1, max = 2048, message = "Motivation must be between 1 and 2048 characters")
    private String motivation;

    @XmlElement
    @NotNull(message = "State cannot be null")
    private ProjectStateEnum state;

    @XmlElement
    @NotNull(message = "Laboratory ID cannot be null")
    @Min(value = 1, message = "Laboratory Id must be greater than 0")
    private Long laboratoryId;

    @XmlElement
    private Instant conclusionDate;

    // Default constructor
    public ProjectUpdateDto() {
    }

    // Parameterized constructor
    public ProjectUpdateDto(Long id, String name, String description, String motivation, ProjectStateEnum state, Long laboratoryId, Instant conclusionDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.state = state;
        this.laboratoryId = laboratoryId;
        this.conclusionDate = conclusionDate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public ProjectStateEnum getState() {
        return state;
    }

    public void setState(ProjectStateEnum state) {
        this.state = state;
    }

    public Long getLaboratoryId() {
        return laboratoryId;
    }

    public void setLaboratoryId(Long laboratoryId) {
        this.laboratoryId = laboratoryId;
    }

    public Instant getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(Instant conclusionDate) {
        this.conclusionDate = conclusionDate;
    }
}
