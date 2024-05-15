package aor.fpbackend.dao;

import aor.fpbackend.entity.RoleEntity;

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

    public RoleEntity findRoleById(Long roleId) {
        try {
            return (RoleEntity) em.createNamedQuery("Role.findRoleById")
                    .setParameter("roleId", roleId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
