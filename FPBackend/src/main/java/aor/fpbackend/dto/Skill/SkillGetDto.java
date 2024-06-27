package aor.fpbackend.dto;

import aor.fpbackend.enums.SkillTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class SkillGetDto implements Serializable {
    @XmlElement
    private long id;
    @XmlElement
    private String name;
    @XmlElement
    @Enumerated
    private SkillTypeEnum type;

    public SkillGetDto() {
    }

    public SkillGetDto(long id, String name, SkillTypeEnum type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public SkillTypeEnum getType() {
        return type;
    }

    public void setType(SkillTypeEnum type) {
        this.type = type;
    }
}
