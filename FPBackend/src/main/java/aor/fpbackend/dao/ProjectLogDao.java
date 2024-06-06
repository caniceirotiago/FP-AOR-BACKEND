package aor.fpbackend.dao;

import aor.fpbackend.entity.ProjectLogEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;

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
}
