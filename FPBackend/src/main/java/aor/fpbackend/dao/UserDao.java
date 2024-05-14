package aor.fpbackend.dao;

import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {
    private static final long serialVersionUID = 1L;
    public UserDao() {
        super(UserEntity.class);
    }
    @PersistenceContext
    private EntityManager entityManager;

//    public UserEntity findUserByToken(String token) {
//        try {
//            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
//                    .getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }
//    }

    public boolean checkIfEmailExists(String email) {
        try {
            Query query = entityManager.createNamedQuery("User.checkIfEmailExists");
            Long count = (Long) query.setParameter("email", email).getSingleResult();
            if (count > 0){
                return true;
            } else return false;
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<UserEntity> findAllUsers() {
        try {
            return (List<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();

        } catch (NoResultException e) {
            return null;
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
            return (UserEntity) em.createNamedQuery("User.findUserByNickname").setParameter("nickname", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }


}
