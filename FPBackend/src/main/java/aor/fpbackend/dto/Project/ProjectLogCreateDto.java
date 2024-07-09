package aor.fpbackend.dto.Project;

import aor.fpbackend.enums.LogTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class ProjectLogCreateDto implements Serializable {

    @XmlElement
    @NotEmpty
    private String content;

    public ProjectLogCreateDto() {
    }

    public ProjectLogCreateDto(String content) {
        this.content = content;
    }

    // Getters and setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
