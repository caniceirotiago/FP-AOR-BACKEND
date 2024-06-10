package aor.fpbackend.dao;

import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.ProjectMembershipEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class ProjectMembershipDao extends AbstractDao<ProjectMembershipEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectMembershipDao() {
        super(ProjectMembershipEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public ProjectMembershipEntity findProjectMembershipById(long id) {
        try {
            return (ProjectMembershipEntity) em.createNamedQuery("ProjectMembership.findProjectMembershipById")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProjectMembershipEntity findProjectMembershipByAcceptanceToken(String acceptanceToken) {
        try {
            return (ProjectMembershipEntity) em.createNamedQuery("ProjectMembership.findProjectMembershipByAcceptanceToken")
                    .setParameter("acceptanceToken", acceptanceToken)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProjectMembershipEntity findProjectMembershipByUserIdAndProjectId(long projectId, long userId) {
        try {
            return (ProjectMembershipEntity) em.createNamedQuery("ProjectMembership.findProjectMembershipByProjectIdAndUserId")
                    .setParameter("projectId", projectId)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean isUserProjectManager(long projectId, long userId) {
        try {
            em.createNamedQuery("ProjectMembership.findProjectMembershipByProjectIdAndUserIdAndRole", ProjectMembershipEntity.class)
                    .setParameter("projectId", projectId)
                    .setParameter("userId", userId)
                    .setParameter("role", "PROJECT_MANAGER")
                    .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<UserEntity> findProjectManagers(long projectId) {
        return em.createNamedQuery("ProjectMembership.findProjectManagers")
                .setParameter("projectId", projectId)
                .getResultList();
    }
}
