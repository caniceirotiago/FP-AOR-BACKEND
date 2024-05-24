package aor.fpbackend.dao;


import aor.fpbackend.entity.InterestEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

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


    public List<SkillEntity> getAllSkills() {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s", SkillEntity.class);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByUserId(long userId) {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT s FROM SkillEntity s JOIN s.users u WHERE u.id = :userId", SkillEntity.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<SkillEntity> getSkillsByFirstLetter(char firstLetter) {
        TypedQuery<SkillEntity> query = em.createQuery(
                "SELECT s FROM SkillEntity s WHERE s.name LIKE :pattern", SkillEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }
}
