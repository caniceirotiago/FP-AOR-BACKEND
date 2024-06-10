package aor.fpbackend.dto;

import aor.fpbackend.enums.AssetTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class AssetAddDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Name must start with a letter")
    private String name;

    @XmlElement
    @NotNull
    @Min(value = 0, message = "quantity must be greater than 0")
    private int usedQuantity;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;


    public AssetAddDto() {
    }

    public AssetAddDto(String name, int usedQuantity, long projectId) {
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