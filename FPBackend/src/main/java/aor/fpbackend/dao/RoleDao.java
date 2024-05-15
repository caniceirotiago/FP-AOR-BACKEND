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

    public void createRoleIfNotExists(String roleName) {
        List<RoleEntity> roles = em.createQuery(
                        "SELECT r FROM RoleEntity r WHERE r.name = :roleName", RoleEntity.class)
                .setParameter("roleName", roleName)
                .getResultList();

        if (roles.isEmpty()) {
            RoleEntity role = new RoleEntity(roleName);
            em.persist(role);
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

}
