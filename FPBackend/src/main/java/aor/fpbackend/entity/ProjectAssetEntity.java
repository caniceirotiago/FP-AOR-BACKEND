package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "project_asset")
public class ProjectAssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(name = "used_quantity", nullable = false)
    private Integer usedQuantity;

    public ProjectAssetEntity() {}

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public AssetEntity getAsset() {
        return asset;
    }

    public void setAsset(AssetEntity asset) {
        this.asset = asset;
    }

    public Integer getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(Integer usedQuantity) {
        this.usedQuantity = usedQuantity;
    }


    // toString method


    @Override
    public String toString() {
        return "ProjectAssetEntity{" +
                "id=" + id +
                ", project=" + project +
                ", asset=" + asset +
                ", usedQuantity=" + usedQuantity +
                '}';
    }
}
