package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class SkillRemoveProjectDto implements Serializable {
    @XmlElement
    private long id;

    @XmlElement
    @NotNull
    private long projectId;

    public SkillRemoveProjectDto() {
    }

    public SkillRemoveProjectDto(long id, long projectId) {
        this.id = id;
        this.projectId = projectId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}