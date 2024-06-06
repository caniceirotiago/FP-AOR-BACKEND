package aor.fpbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class ProjectAskJoinDto implements Serializable {
    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;
    @XmlElement
    @Min(value = 1, message = "ID must be greater than 0")
    @NotNull
    private long userId;

    public ProjectAskJoinDto(long projectId, long userId) {
        this.projectId = projectId;
        this.userId = userId;
    }
    public ProjectAskJoinDto() {
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
    //TODO validations
}
