package aor.fpbackend.dao;

import aor.fpbackend.bean.PassEncoder;
import aor.fpbackend.dto.ProjectMembershipDto;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {
    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkEmailExist(String email) {
        try {
            Long count = (Long) em.createNamedQuery("User.countUserByEmail")
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean checkUsernameExist(String username) {
        try {
            Long count = (Long) em.createNamedQuery("User.countUserByUsername")
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
    public  boolean confirmUserIdExists(String userId) {
        try {
            Long count = (Long) em.createNamedQuery("User.countUserById")
                    .setParameter("userId", Long.parseLong(userId))
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public UserEntity findUserById(long userId) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserById")
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByToken(String token) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByConfirmationToken(String token) {
        try {
            Query query = em.createNamedQuery("User.findUserByConfirmationToken", UserEntity.class);
            query.setParameter("confirmationToken", token);
            return (UserEntity) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByResetPasswordToken(String resetPasswordToken) {
        try {
            return em.createNamedQuery("User.findUserByResetPasswordToken", UserEntity.class)
                    .setParameter("resetPasswordToken", resetPasswordToken)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Token inválido ou expirado
        }
    }

    public UserEntity findUserByEmail(String email) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<UserEntity> findAllUsers() {
        try {
            return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> getUsersByFirstLetter(String firstLetter) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKE :pattern", UserEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }
    public List<ProjectMembershipDto> getUsersByProject(long projectId) {
        TypedQuery<ProjectMembershipDto> query = em.createNamedQuery("ProjectMembership.findProjectMembershipsByProject", ProjectMembershipDto.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }
}
