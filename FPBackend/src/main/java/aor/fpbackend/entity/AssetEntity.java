package aor.fpbackend.entity;

import aor.fpbackend.enums.AssetTypeEnum;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asset")

@NamedQuery(name = "Asset.countAssetByName", query = "SELECT COUNT(a) FROM AssetEntity a WHERE LOWER(a.name) = LOWER(:name)")
@NamedQuery(name = "Asset.countAssetByPartNumber", query = "SELECT COUNT(a) FROM AssetEntity a WHERE LOWER(a.partNumber) = LOWER(:partNumber)")
@NamedQuery(name = "Asset.findAssetByName", query = "SELECT a FROM AssetEntity a WHERE LOWER(a.name) = LOWER(:name)")
@NamedQuery(name = "Asset.findAssetById", query = "SELECT a FROM AssetEntity a WHERE a.id = :assetId")

public class AssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AssetTypeEnum type;

    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "manufacturer_phone", nullable = false)
    private String manufacturerPhone;

    @Column(name = "observations", nullable = true, length = 2048)
    private String observations;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectAssetEntity> projectAssets = new HashSet<>();

    public AssetEntity() {}

    public AssetEntity(String name, AssetTypeEnum type, String description, int stockQuantity, String partNumber, String manufacturer, String manufacturerPhone, String observations) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.partNumber = partNumber;
        this.manufacturer = manufacturer;
        this.manufacturerPhone = manufacturerPhone;
        this.observations = observations;
    }

    // Getters and setters

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

    public void setStockQuantity(int quantity) {
        this.stockQuantity = quantity;
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

    public Set<ProjectAssetEntity> getProjectAssets() {
        return projectAssets;
    }

    public void setProjectAssets(Set<ProjectAssetEntity> projectAssets) {
        this.projectAssets = projectAssets;
    }


    // toString method

    @Override
    public String toString() {
        return "AssetEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + stockQuantity +
                ", partNumber='" + partNumber + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", manufacturerPhone='" + manufacturerPhone + '\'' +
                ", observations='" + observations + '\'' +
                ", projectAssets=" + projectAssets +
                '}';
    }
}
