package aor.fpbackend.dto.Skill;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class SkillRemoveUserDto implements Serializable {
    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long id;

    public SkillRemoveUserDto() {
    }

    public SkillRemoveUserDto(long id) {
        this.id = id;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
