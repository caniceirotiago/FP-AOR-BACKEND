package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

    @Stateless
    public class LaboratoryDao extends AbstractDao<LaboratoryEntity> {
        private static final long serialVersionUID = 1L;

        public LaboratoryDao() {
            super(LaboratoryEntity.class);
        }

        @PersistenceContext
        private EntityManager em;

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
