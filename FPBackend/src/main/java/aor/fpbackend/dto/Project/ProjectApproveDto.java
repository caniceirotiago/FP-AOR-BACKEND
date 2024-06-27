package aor.fpbackend.dto.Project;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ProjectApproveDto implements Serializable {


    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;

    @XmlElement
    @NotNull
    private boolean confirm;

    @XmlElement
    @NotNull
    private String comment;


    public ProjectApproveDto() {
    }

    public ProjectApproveDto(long projectId, boolean confirm, String comment) {
        this.projectId = projectId;
        this.confirm = confirm;
        this.comment = comment;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}