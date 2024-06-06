package aor.fpbackend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class ProjectInviteDto implements Serializable {

    @XmlElement
    @NotNull
    private String username;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "Project Id must be greater than 0")
    private long projectId;

    public ProjectInviteDto() {
    }

    public ProjectInviteDto(String username, long projectId) {
        this.username = username;
        this.projectId = projectId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}