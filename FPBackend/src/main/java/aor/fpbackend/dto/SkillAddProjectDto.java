package aor.fpbackend.dto;

import aor.fpbackend.enums.SkillTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class SkillAddProjectDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Name must start with a letter")
    private String name;

    @XmlElement
    @Enumerated
    private SkillTypeEnum type;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;

    public SkillAddProjectDto() {
    }

    public SkillAddProjectDto(String name, SkillTypeEnum type, long projectId) {
        this.name = name;
        this.type = type;
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillTypeEnum getType() {
        return type;
    }

    public void setType(SkillTypeEnum type) {
        this.type = type;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
