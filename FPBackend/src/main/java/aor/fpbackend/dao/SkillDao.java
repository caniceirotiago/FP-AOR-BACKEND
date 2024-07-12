package aor.fpbackend.dao;


import aor.fpbackend.entity.SkillEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
/**
 * SkillDao class provides data access operations for {@link SkillEntity}.
 * <br>
 * This class implements methods to perform CRUD operations and custom queries
 * related to skills in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for skill management.
 * <br>
 */
@Stateless
public class SkillDao extends AbstractDao<SkillEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public SkillDao() {
        super(SkillEntity.class);
    }

    public boolean checkSkillExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Skill.countSkillByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public SkillEntity findSkillByName(String name) {
        try {
            return (SkillEntity) em.createNamedQuery("Skill.findSkillByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public SkillEntity findSkillById(long skillId) {
        try {
            return (SkillEntity) em.createNamedQuery("Skill.findSkillById")
                    .setParameter("skillId", skillId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public List<SkillEntity> getAllSkills() {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s", SkillEntity.class);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByUserId(long userId) {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s JOIN s.users u WHERE u.id = :userId ORDER BY s.name", SkillEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByUsername(String username) {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s JOIN s.users u WHERE u.username = :username ORDER BY s.name", SkillEntity.class);
        query.setParameter("username", username);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByProjectId(long projectId) {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s JOIN s.projects p WHERE p.id = :projectId ORDER BY s.name", SkillEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByFirstLetter(String firstLetter) {
        TypedQuery<SkillEntity> query = em.createQuery(
                "SELECT s FROM SkillEntity s WHERE LOWER(s.name) LIKE :pattern", SkillEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }
}
