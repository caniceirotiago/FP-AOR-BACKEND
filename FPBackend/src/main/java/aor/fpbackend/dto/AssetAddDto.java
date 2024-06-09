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
    @Enumerated
    private AssetTypeEnum type;

    @XmlElement
    private String description;

    @XmlElement
    @Min(value = 1, message = "quantity must be greater than 0")
    private int stockQuantity;

    @XmlElement
    @Min(value = 0, message = "quantity must be greater than 0")
    private int usedQuantity;

    @XmlElement
    private String partNumber;

    @XmlElement
    private String manufacturer;

    @XmlElement
    private String manufacturerPhone;

    @XmlElement
    private String observations;

    @XmlElement
    @Min(value = 1, message = "ID must be greater than 0")
    private long projectId;


    public AssetAddDto() {
    }

    public AssetAddDto(String name, AssetTypeEnum type, String description, int stockQuantity, int usedQuantity, String partNumber, String manufacturer, String manufacturerPhone, String observations, long projectId) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.usedQuantity = usedQuantity;
        this.partNumber = partNumber;
        this.manufacturer = manufacturer;
        this.manufacturerPhone = manufacturerPhone;
        this.observations = observations;
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetTypeEnum getType() {
        return type;
    }

    public void setType(AssetTypeEnum type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturerPhone() {
        return manufacturerPhone;
    }

    public void setManufacturerPhone(String manufacturerPhone) {
        this.manufacturerPhone = manufacturerPhone;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}