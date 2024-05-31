package aor.fpbackend.dto;

import aor.fpbackend.entity.ProjectMembershipEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.entity.LaboratoryEntity;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@XmlRootElement
public class ProjectGetDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private String motivation;

    @XmlElement
    private ProjectStateEnum state;

    @XmlElement
    private Instant creationDate;

    @XmlElement
    private Instant initialDate;

    @XmlElement
    private Instant finalDate;

    @XmlElement
    private Instant conclusionDate;

    @XmlElement
    private LaboratoryDto laboratory;

    @XmlElement
    private List<ProjectMembershipDto> members;
    @XmlElement
    private UserBasicInfoDto createdBy;


    public ProjectGetDto() {
    }

    public ProjectGetDto(long id, String name, String description, String motivation, ProjectStateEnum state, Instant creationDate, Instant initialDate, Instant finalDate, Instant conclusionDate, LaboratoryDto laboratory, List<ProjectMembershipDto> members, UserBasicInfoDto createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.state = state;
        this.creationDate = creationDate;
        this.initialDate = initialDate;
        this.finalDate = finalDate;
        this.conclusionDate = conclusionDate;
        this.laboratory = laboratory;
        this.members = members;
        this.createdBy = createdBy;
    }

    // Getters and setters

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

    public ProjectStateEnum getState() {
        return state;
    }

    public void setState(ProjectStateEnum state) {
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


    public LaboratoryDto getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(LaboratoryDto laboratory) {
        this.laboratory = laboratory;
    }

    public List<ProjectMembershipDto> getMembers() {
        return members;
    }

    public void setMembers(List<ProjectMembershipDto> members) {
        this.members = members;
    }

    public UserBasicInfoDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserBasicInfoDto createdBy) {
        this.createdBy = createdBy;
    }
}
