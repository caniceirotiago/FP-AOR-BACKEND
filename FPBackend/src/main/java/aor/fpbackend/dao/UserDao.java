package aor.fpbackend.dao;

import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
/**
 * UserDao class provides data access operations for {@link UserEntity}.
 * <p>
 * This class implements methods to perform CRUD operations and custom queries
 * related to users in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for user management.
 * <br>
 */
@Stateless
public class UserDao extends AbstractDao<UserEntity> {
    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkEmailAndUsernameExist(String email, String username) {
        try {
            Long count = (Long) em.createNamedQuery("User.countUserByEmailAndUsername")
                    .setParameter("email", email)
                    .setParameter("username", username)
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

    public UserEntity findValidatedUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findValidatedUserByUsername").setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<UserEntity> findUsersSettingsPage(Long loggedId) {
        try {
            return (ArrayList<UserEntity>) em.createNamedQuery("User.findUsersSettingsPage")
                    .setParameter("loggedId", loggedId)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public List<UserEntity> getUsersByFirstLetter(String firstLetter) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKE :pattern AND u.isConfirmed = true", UserEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }

    public List<UserEntity> getUsersByFirstLetterUsernameOrFirstName(String firstLetter) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u WHERE (LOWER(u.username) LIKE :pattern OR LOWER(u.firstName) LIKE :pattern) AND u.isConfirmed = true", UserEntity.class);
        query.setParameter("pattern", firstLetter.toLowerCase() + "%");
        return query.getResultList();
    }

    public List<UserEntity> getUsersByRole(RoleEntity role) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT u FROM UserEntity u JOIN u.role r WHERE r = :role", UserEntity.class);
        query.setParameter("role", role);
        return query.getResultList();
    }
}
