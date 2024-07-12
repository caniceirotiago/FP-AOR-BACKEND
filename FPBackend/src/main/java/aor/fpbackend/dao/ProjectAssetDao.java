package aor.fpbackend.dao;

import aor.fpbackend.entity.ProjectAssetEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;
/**
 * ProjectAssetDao class provides data access operations for {@link ProjectAssetEntity}.
 * <p>
 * This class implements methods to perform CRUD operations and custom queries
 * related to project assets in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for project asset management.
 * <br>
 */
@Stateless
public class ProjectAssetDao extends AbstractDao<ProjectAssetEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectAssetDao() {
        super(ProjectAssetEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public ProjectAssetEntity findProjectAssetById(long id) {
        try {
            return (ProjectAssetEntity) em.createNamedQuery("ProjectAsset.findProjectAssetById")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ProjectAssetEntity> findProjectAssetsByProjectId(long projectId) {
        return em.createNamedQuery("ProjectAsset.findProjectAssetsByProjectId")
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public boolean checkAssetInUse(long assetId) {
        try {
            Long count = (Long) em.createNamedQuery("ProjectAsset.countProjectAssetsByAssetId")
                    .setParameter("assetId", assetId)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    // Methods for Asset Report
    public List<Object[]> getTopProjectsByUsedQuantity() {
        return em.createNamedQuery("ProjectAsset.getTopProjectsByUsedQuantity")
                .setMaxResults(5) // Limiting to top 5 results
                .getResultList();
    }

    public List<Object[]> getTopAssetsByUsedQuantity() {
        return em.createNamedQuery("ProjectAsset.getTopAssetsByUsedQuantity", Object[].class)
                .setMaxResults(5) // Limiting to top 5 results
                .getResultList();
    }

}
