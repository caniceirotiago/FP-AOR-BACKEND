package aor.fpbackend.dao;

import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

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

    public UserEntity findUserByNickname(String nickname) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByNickname").setParameter("nickname", nickname)
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

}
