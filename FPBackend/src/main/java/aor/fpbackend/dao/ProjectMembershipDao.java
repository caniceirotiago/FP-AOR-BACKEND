package aor.fpbackend.dao;

import aor.fpbackend.dto.Project.ProjectMembershipDto;
import aor.fpbackend.entity.ProjectMembershipEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.ProjectRoleEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class ProjectMembershipDao extends AbstractDao<ProjectMembershipEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectMembershipDao() {
        super(ProjectMembershipEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public List<UserEntity> findProjectMembersByProjectId(Long projectId) {
        TypedQuery<UserEntity> query = em.createQuery("SELECT pm.user FROM ProjectMembershipEntity pm " +
                "WHERE pm.project.id = :projectId", UserEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
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

    public boolean isUserProjectMember(long projectId, long userId) {
        try {
            em.createNamedQuery("ProjectMembership.isUserProjectMember", ProjectMembershipEntity.class)
                    .setParameter("projectId", projectId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean isUserProjectManager(long projectId, long userId) {
        try {
            em.createNamedQuery("ProjectMembership.findProjectMembershipByProjectIdAndUserIdAndRole", ProjectMembershipEntity.class)
                    .setParameter("projectId", projectId)
                    .setParameter("userId", userId)
                    .setParameter("role", ProjectRoleEnum.PROJECT_MANAGER)
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

    public List<Long> findProjectIdsByUserId(long userId) {
        return em.createNamedQuery("ProjectMembership.findProjectIdsByUserId")
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<UserEntity> findUsersByFirstLetterAndProjId(String firstLetter, long projectId) {
        return em.createNamedQuery("ProjectMembership.findUsersByFirstLetterAndProjId", UserEntity.class)
                .setParameter("firstLetter", firstLetter.toLowerCase() + "%")
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public List<ProjectMembershipDto> getUsersByProject(long projectId) {
        TypedQuery<ProjectMembershipDto> query = em.createNamedQuery("ProjectMembership.findProjectMembershipsByProject", ProjectMembershipDto.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

}
