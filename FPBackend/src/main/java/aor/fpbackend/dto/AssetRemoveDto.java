package aor.fpbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class AssetRemoveDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long id;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;

    public AssetRemoveDto() {
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
