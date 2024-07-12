package aor.fpbackend.dao;

import aor.fpbackend.entity.ProjectLogEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
/**
 * ProjectLogDao class provides data access operations for {@link ProjectLogEntity}.
 * <br>
 * This class implements methods to perform CRUD operations and custom queries
 * related to project logs in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for project log management.
 * <br>
 */
@Stateless
public class ProjectLogDao extends AbstractDao<ProjectLogEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectLogDao() {
        super(ProjectLogEntity.class);
    }

    @PersistenceContext
    private EntityManager em;


    public ArrayList<ProjectLogEntity> findAllProjectLogs() {
        try {
            return (ArrayList<ProjectLogEntity>) em.createNamedQuery("ProjectLog.findAllProjectLogs").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ProjectLogEntity> findProjectLogsByProjectId(long projectId) {
        try {
            return em.createNamedQuery("ProjectLog.findProjectLogsByProjectId", ProjectLogEntity.class)
                    .setParameter("projectId", projectId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
