package aor.fpbackend.dao;

import aor.fpbackend.entity.ProjectAssetEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

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
    // Get used quantity by project
    public List<Object[]> getUsedQuantityByProject() {
        return em.createNamedQuery("ProjectAsset.getUsedQuantityByProject", Object[].class)
                .getResultList();
    }

    // Get used quantity by asset type
    public List<Object[]> getUsedQuantityByAssetType() {
        return em.createNamedQuery("ProjectAsset.getUsedQuantityByAssetType")
                .getResultList();
    }

}
