package aor.fpbackend.dao;


import aor.fpbackend.entity.SkillEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
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

    public List<SkillEntity> getAllSkills() {
        TypedQuery<SkillEntity> query = em.createQuery("SELECT i FROM SkillEntity i", SkillEntity.class);
        return query.getResultList();
    }

    public void createSkill(String name) {
        SkillEntity newSkill = new SkillEntity(name);
        em.persist(newSkill);
    }
}
