package aor.fpbackend.dao;

import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.ProjectMembershipEntity;
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
public class ProjectMembershipDao extends AbstractDao<ProjectMembershipEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectMembershipDao() {
        super(ProjectMembershipEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    public ProjectMembershipEntity findProjectMembershipId(long id) {
        try {
            return (ProjectMembershipEntity) em.createNamedQuery("ProjectMembership.findProjectMembershipId")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
