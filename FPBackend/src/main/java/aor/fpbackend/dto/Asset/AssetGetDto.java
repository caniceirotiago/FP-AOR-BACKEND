package aor.fpbackend.dto;

import aor.fpbackend.enums.AssetTypeEnum;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class AssetGetDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private String name;

    @XmlElement
    private AssetTypeEnum type;

    @XmlElement
    private String description;

    @XmlElement
    private int stockQuantity;

    @XmlElement
    private String partNumber;

    @XmlElement
    private String manufacturer;

    @XmlElement
    private String manufacturerPhone;

    @XmlElement
    private String observations;


    public AssetGetDto() {
    }

    public AssetGetDto(long id, String name, AssetTypeEnum type, String description, int stockQuantity, String partNumber, String manufacturer, String manufacturerPhone, String observations) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.partNumber = partNumber;
        this.manufacturer = manufacturer;
        this.manufacturerPhone = manufacturerPhone;
        this.observations = observations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
