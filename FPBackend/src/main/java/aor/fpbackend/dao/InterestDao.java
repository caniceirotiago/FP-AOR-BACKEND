package aor.fpbackend.dao;


import aor.fpbackend.entity.InterestEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class InterestDao extends AbstractDao<InterestEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public InterestDao() {
        super(InterestEntity.class);
    }

    public List<InterestEntity> getAllInterests() {
        TypedQuery<InterestEntity> query = em.createQuery("SELECT i FROM InterestEntity i", InterestEntity.class);
        return query.getResultList();
    }

    public void createInterest(String name) {
        InterestEntity newInterest = new InterestEntity(name);
        em.persist(newInterest);
    }
}
