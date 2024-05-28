package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;


@Stateless
    public class ProjectDao extends AbstractDao<ProjectEntity> {
        private static final long serialVersionUID = 1L;

        public ProjectDao() {
            super(ProjectEntity.class);
        }

        @PersistenceContext
        private EntityManager em;


    public boolean isProjectMember(long projectId, long userId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(m) FROM ProjectMembershipEntity m WHERE m.project.id = :projectId AND m.user.id = :userId", Long.class);
        query.setParameter("projectId", projectId);
        query.setParameter("userId", userId);
        Long count = query.getSingleResult();
        return count > 0;
    }

    public ProjectEntity findProjectById(long projectId) {
        try {
            return (ProjectEntity) em.createNamedQuery("Project.findProjectById")
                    .setParameter("projectId", projectId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProjectEntity findProjectByName(String name) {
        try {
            return (ProjectEntity) em.createNamedQuery("Project.findProjectByName")
                    .setParameter("name", name)
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
