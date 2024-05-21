package aor.fpbackend.dao;

import aor.fpbackend.entity.RoleEntity;

import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class RoleDao extends AbstractDao<RoleEntity> {
    private static final long serialVersionUID = 1L;

    public RoleDao() {
        super(RoleEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public boolean checkRoleExist(UserRoleEnum name) {
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

    public RoleEntity findRoleByName(UserRoleEnum name) {
        try {
            return (RoleEntity) em.createNamedQuery("Role.findRoleByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean isMethodAssociatedWithRole(long roleId, MethodEnum method) {
        try {
            Long count = (Long) em.createNamedQuery("Role.isMethodAssociatedWithRole")
                    .setParameter("roleId", roleId)
                    .setParameter("method", method)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
}
