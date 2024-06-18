package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.enums.LocationEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;

@Stateless
public class LaboratoryDao extends AbstractDao<LaboratoryEntity> {
    private static final long serialVersionUID = 1L;

    public LaboratoryDao() {
        super(LaboratoryEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkLaboratoryExist(LocationEnum location) {
        try {
            Long count = (Long) em.createNamedQuery("Laboratory.countLaboratoryByLocation")
                    .setParameter("location", location)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public LaboratoryEntity findLaboratoryById(long labId) {
        try {
            return (LaboratoryEntity) em.createNamedQuery("Laboratory.findLaboratoryById")
                    .setParameter("labId", labId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<LaboratoryEntity> findAllLaboratories() {
        try {
            return (ArrayList<LaboratoryEntity>) em.createNamedQuery("Laboratory.findAllLaboratories").getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }


}
