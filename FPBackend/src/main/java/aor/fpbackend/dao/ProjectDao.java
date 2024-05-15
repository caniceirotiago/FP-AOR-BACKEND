package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;


@Stateless
    public class ProjectDao extends AbstractDao<ProjectEntity> {
        private static final long serialVersionUID = 1L;

        public ProjectDao() {
            super(ProjectEntity.class);
        }

        @PersistenceContext
        private EntityManager em;


    public ProjectEntity findProjectById(long projectId) {
        try {
            return (ProjectEntity) em.createNamedQuery("Project.findProjectById")
                    .setParameter("projectId", projectId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<ProjectEntity> findAllProjects() {
        try {
            return (ArrayList<ProjectEntity>) em.createNamedQuery("Project.findAllProjects").getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }
}
