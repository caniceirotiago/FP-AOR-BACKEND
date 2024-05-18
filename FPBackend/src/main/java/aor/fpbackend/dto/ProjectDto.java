package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import aor.fpbackend.enums.ProjectState;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class ProjectDto implements Serializable {

    @XmlElement
    @NotNull
    private long id;

    @XmlElement
    @NotNull
    private String name;

    @XmlElement
    @NotNull
    private String description;

    @XmlElement
    @NotNull
    private String motivation;

    @XmlElement
    @NotNull
    private ProjectState state;

    @XmlElement
    @NotNull
    private Instant creationDate;

    @XmlElement
    private Instant initialDate;

    @XmlElement
    @NotNull
    private Instant finalDate;

    @XmlElement
    private Instant conclusionDate;

    @XmlElement
    @NotNull
    private int membersCount;

    public ProjectDto() {
    }

    public ProjectDto(long id, String name, String description, String motivation, ProjectState state, Instant creationDate, Instant initialDate, Instant finalDate, Instant conclusionDate, int membersCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.state = state;
        this.creationDate = creationDate;
        this.initialDate = initialDate;
        this.finalDate = finalDate;
        this.conclusionDate = conclusionDate;
        this.membersCount = membersCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public ProjectState getState() {
        return state;
    }

    public void setState(ProjectState state) {
        this.state = state;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Instant initialDate) {
        this.initialDate = initialDate;
    }

    public Instant getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(Instant finalDate) {
        this.finalDate = finalDate;
    }

    public Instant getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(Instant conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }
}
