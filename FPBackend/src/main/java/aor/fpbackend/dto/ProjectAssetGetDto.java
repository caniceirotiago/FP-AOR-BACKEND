package aor.fpbackend.dto;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ProjectAssetGetDto implements Serializable {

    @XmlElement
    private String name;

    @XmlElement
    private int usedQuantity;


    public ProjectAssetGetDto() {
    }

    public ProjectAssetGetDto(String name, int usedQuantity) {
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