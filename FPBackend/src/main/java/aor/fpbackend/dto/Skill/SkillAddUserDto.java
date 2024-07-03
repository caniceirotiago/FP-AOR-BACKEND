package aor.fpbackend.dto.Skill;

import aor.fpbackend.enums.SkillTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class SkillAddUserDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    private String name;

    @XmlElement
    @Enumerated
    @NotNull
    private SkillTypeEnum type;

    public SkillAddUserDto() {
    }

    public SkillAddUserDto(String name, SkillTypeEnum type) {
        this.name = name;
        this.type = type;
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
}

