package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class LaboratoryDao extends AbstractDao<LaboratoryEntity> {
    private static final long serialVersionUID = 1L;

    public LaboratoryDao() {
        super(LaboratoryEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public void createLaboratoryIfNotExists(String location) {
        List<LaboratoryEntity> labs = em.createQuery(
                        "SELECT l FROM LaboratoryEntity l WHERE l.location = :location", LaboratoryEntity.class)
                .setParameter("location", location)
                .getResultList();

        if (labs.isEmpty()) {
            LaboratoryEntity lab = new LaboratoryEntity(location);
            em.persist(lab);
        }
    }

    public LaboratoryEntity findLaboratoryById(Long labId) {
        try {
            return (LaboratoryEntity) em.createNamedQuery("Laboratory.findLaboratoryById")
                    .setParameter("labId", labId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


}
