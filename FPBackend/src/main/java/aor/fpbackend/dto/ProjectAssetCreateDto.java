package aor.fpbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectAssetCreateDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Name must start with a letter")
    private String name;

    @XmlElement
    private int usedQuantity;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;


    public ProjectAssetCreateDto() {
    }

    public ProjectAssetCreateDto(String name, int usedQuantity, long projectId) {
        this.name = name;
        this.usedQuantity = usedQuantity;
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}