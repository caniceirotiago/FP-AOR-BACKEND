package aor.fpbackend.dto;

import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;


@XmlRootElement
public class ProjectCreateDto implements Serializable {

    @XmlElement
    @NotNull
    private String name;

    @XmlElement
    @NotNull
    private String description;

    @XmlElement
    private String motivation;

    @XmlElement
    private Instant conclusionDate;

    @XmlElement
    private long laboratoryId;

    public ProjectCreateDto() {
    }

    public ProjectCreateDto(String name, String description, String motivation, Instant conclusionDate, long laboratoryId) {
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.conclusionDate = conclusionDate;
        this.laboratoryId = laboratoryId;
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

    public Instant getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(Instant conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public long getLaboratoryId() {
        return laboratoryId;
    }

    public void setLaboratoryId(long laboratoryId) {
        this.laboratoryId = laboratoryId;
    }
}