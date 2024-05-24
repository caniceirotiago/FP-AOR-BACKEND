package aor.fpbackend.dao;

import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.RoleEntity;

import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.HashSet;
import java.util.Set;

@Stateless
public class RoleDao extends AbstractDao<RoleEntity> {
    private static final long serialVersionUID = 1L;

    public RoleDao() {
        super(RoleEntity.class);
    }

    @PersistenceContext
    private EntityManager em;
    public Set<MethodEntity> findPermissionsByRoleId(Long roleId) {
        try {
            return em.createQuery("SELECT r FROM RoleEntity r JOIN FETCH r.methods WHERE r.id = :roleId", RoleEntity.class)
                    .setParameter("roleId", roleId)
                    .getSingleResult()
                    .getMethods();
        } catch (NoResultException e) {
            return new HashSet<>();
        }
    }

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
