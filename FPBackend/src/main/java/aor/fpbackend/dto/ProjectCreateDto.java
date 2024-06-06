package aor.fpbackend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@XmlRootElement
public class ProjectCreateDto implements Serializable {

    @XmlElement
    @NotNull
    @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
    private String name;

    @XmlElement
    @NotNull
    @Size(min = 2, max = 2048, message = "Description must be between 2 and 2048 characters")
    private String description;

    @XmlElement
    @NotNull
    @Size(min = 2, max = 2048, message = "Motivation must be between 2 and 2048 characters")
    private String motivation;

    @XmlElement
    private Instant conclusionDate;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "Laboratory ID must be greater than 0")
    private long laboratoryId;

    @XmlElement
    private Set<SkillAddProjectDto> skills;

    @XmlElement
    private Set<KeywordAddDto> keywords;

    @XmlElement
    private Set<UsernameDto> users;


    public ProjectCreateDto() {
    }

    public ProjectCreateDto(String name, String description, String motivation, Instant conclusionDate, long laboratoryId) {
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.conclusionDate = conclusionDate;
        this.laboratoryId = laboratoryId;
        this.skills = new HashSet<>();
        this.keywords = new HashSet<>();
        this.users = new HashSet<>();
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

    public Set<SkillAddProjectDto> getSkills() {
        return skills;
    }

    public void setSkills(Set<SkillAddProjectDto> skills) {
        this.skills = skills;
    }

    public Set<KeywordAddDto> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<KeywordAddDto> keywords) {
        this.keywords = keywords;
    }

    public Set<UsernameDto> getUsers() {
        return users;
    }

    public void setUsers(Set<UsernameDto> users) {
        this.users = users;
    }
}