package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "project_asset")

@NamedQuery(name = "ProjectAsset.findProjectAssetById", query = "SELECT p FROM ProjectAssetEntity p WHERE p.id = :id")
@NamedQuery(name = "ProjectAsset.findProjectAssetsByProjectId", query = "SELECT p FROM ProjectAssetEntity p WHERE p.project.id = :projectId")

public class ProjectAssetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private AssetEntity asset;

    @Column(name = "used_quantity", nullable = false)
    private int usedQuantity;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    public ProjectAssetEntity() {
    }

    public ProjectAssetEntity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

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

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
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
