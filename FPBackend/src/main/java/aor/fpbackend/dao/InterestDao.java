package aor.fpbackend.dao;


import aor.fpbackend.entity.InterestEntity;
import aor.fpbackend.entity.KeywordEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
/**
 * InterestDao class provides data access operations for {@link InterestEntity}.
 * <p>
 * This class implements methods to perform CRUD operations and custom queries
 * related to interests in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for interest management.
 * <br>
 */
@Stateless
public class InterestDao extends AbstractDao<InterestEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public InterestDao() {
        super(InterestEntity.class);
    }

    public boolean checkInterestExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Interest.countInterestByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public InterestEntity findInterestByName(String name) {
        try {
            return (InterestEntity) em.createNamedQuery("Interest.findInterestByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public InterestEntity findInterestById(long interestId) {
        try {
            return (InterestEntity) em.createNamedQuery("Interest.findInterestById")
                    .setParameter("interestId", interestId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<InterestEntity> getInterestsByUserId(long userId) {
        TypedQuery<InterestEntity> query = em.createQuery("SELECT i FROM InterestEntity i JOIN i.users u WHERE u.id = :userId ORDER BY i.name", InterestEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<InterestEntity> getInterestsByUsername(String username) {
        TypedQuery<InterestEntity> query = em.createQuery("SELECT i FROM InterestEntity i JOIN i.users u WHERE u.username = :username ORDER BY i.name", InterestEntity.class);
        query.setParameter("username", username);
        return query.getResultList();
    }

    public List<InterestEntity> getAllInterests() {
        TypedQuery<InterestEntity> query = em.createQuery("SELECT i FROM InterestEntity i", InterestEntity.class);
        return query.getResultList();
    }

    public List<InterestEntity> getInterestsByFirstLetter(String firstLetter) {
        TypedQuery<InterestEntity> query = em.createQuery(
                "SELECT i FROM InterestEntity i WHERE LOWER(i.name) LIKE :pattern", InterestEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }
}
