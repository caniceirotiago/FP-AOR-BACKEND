package aor.fpbackend.dao;

import aor.fpbackend.entity.RoleEntity;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class RoleDao extends AbstractDao<RoleEntity> {
    private static final long serialVersionUID = 1L;

    public RoleDao() {
        super(RoleEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkRoleExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Role.countRoleByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public RoleEntity findRoleById(long roleId) {
        try {
            return (RoleEntity) em.createNamedQuery("Role.findRoleById")
                    .setParameter("roleId", roleId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean isMethodAssociatedWithRole(long roleId, long methodId) {
        try {
            Long count = (Long) em.createNamedQuery("Role.isMethodAssociatedWithRole")
                    .setParameter("roleId", roleId)
                    .setParameter("methodId", methodId)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
}
