package aor.fpbackend.dao;

import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.RoleEntity;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class MethodDao extends AbstractDao<MethodEntity> {
    private static final long serialVersionUID = 1L;

    public MethodDao() {
        super(MethodEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkMethodExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Method.countMethodByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public MethodEntity findMethodById(long methodId) {
        try {
            return (MethodEntity) em.createNamedQuery("Method.findMethodById")
                    .setParameter("methodId", methodId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public MethodEntity findMethodByName(String name) {
        try {
            return (MethodEntity) em.createNamedQuery("Method.findMethodByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

