package aor.fpbackend.dto.Project;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ProjectAssetCreateNewProjectDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    private String name;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "quantity must be greater than 0")
    private int usedQuantity;


    public ProjectAssetCreateNewProjectDto() {
    }

    public ProjectAssetCreateNewProjectDto(String name, int usedQuantity) {
        this.name = name;
        this.usedQuantity = usedQuantity;
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
}