package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asset")
public class AssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;  // Could be "Component" or "Resource"

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "part_number", nullable = true)
    private String partNumber;

    @Column(name = "manufacturer", nullable = true)
    private String manufacturer;

    @Column(name = "manufacturer_phone", nullable = true)
    private String manufacturerPhone;

    @Column(name = "observations", nullable = true, length = 2048)
    private String observations;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectAssetEntity> projectAssets = new HashSet<>();

    public AssetEntity() {}

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
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
                ", totalQuantity=" + totalQuantity +
                ", availableQuantity=" + availableQuantity +
                ", partNumber='" + partNumber + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", manufacturerPhone='" + manufacturerPhone + '\'' +
                ", observations='" + observations + '\'' +
                ", projectAssets=" + projectAssets +
                '}';
    }
}
