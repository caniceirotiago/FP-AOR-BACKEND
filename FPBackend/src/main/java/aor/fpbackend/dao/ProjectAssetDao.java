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
}
