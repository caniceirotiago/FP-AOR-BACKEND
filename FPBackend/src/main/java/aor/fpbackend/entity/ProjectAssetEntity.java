package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "project_asset")

@NamedQuery(name = "ProjectAsset.findProjectAssetById", query = "SELECT pa FROM ProjectAssetEntity pa WHERE pa.id = :id")
@NamedQuery(name = "ProjectAsset.findProjectAssetsByProjectId", query = "SELECT pa FROM ProjectAssetEntity pa WHERE pa.project.id = :projectId")
@NamedQuery(name = "ProjectAsset.countProjectAssetsByAssetId", query = "SELECT COUNT(pa) FROM ProjectAssetEntity pa WHERE pa.asset.id = :assetId")
@NamedQuery(name = "ProjectAsset.getTopProjectsByUsedQuantity", query =
        "SELECT p.name, SUM(CASE WHEN a.type = 'RESOURCE' THEN pa.usedQuantity ELSE 0 END) AS resourceUsedQuantity, " +
                "       SUM(CASE WHEN a.type = 'COMPONENT' THEN pa.usedQuantity ELSE 0 END) AS componentUsedQuantity, " +
                "       SUM(pa.usedQuantity) AS totalUsedQuantity " +
                "FROM ProjectAssetEntity pa JOIN pa.project p JOIN pa.asset a " +
                "GROUP BY p.name ORDER BY totalUsedQuantity DESC")
@NamedQuery(name = "ProjectAsset.getTopAssetsByUsedQuantity", query = "SELECT a.name, a.type, SUM(pa.usedQuantity) AS totalUsedQuantity " +
                "FROM ProjectAssetEntity pa JOIN pa.asset a " +
                "GROUP BY a.name, a.type ORDER BY totalUsedQuantity DESC")

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
