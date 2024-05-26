package aor.fpbackend.dao;

import aor.fpbackend.entity.KeywordEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class KeywordDao extends AbstractDao<KeywordEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public KeywordDao() {
        super(KeywordEntity.class);
    }

    public boolean checkKeywordExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Keyword.countKeywordByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public KeywordEntity findKeywordByName(String name) {
        try {
            return (KeywordEntity) em.createNamedQuery("Keyword.findKeywordByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public KeywordEntity findKeywordById(long keyId) {
        try {
            return (KeywordEntity) em.createNamedQuery("Keyword.findKeywordById")
                    .setParameter("keyId", keyId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public List<KeywordEntity> getAllKeywords() {
        TypedQuery<KeywordEntity> query = em.createQuery("SELECT k FROM KeywordEntity k", KeywordEntity.class);
        return query.getResultList();
    }

    public List<KeywordEntity> getKeywordsByProjectId(long projectId) {
        TypedQuery<KeywordEntity> query = em.createQuery("SELECT k FROM KeywordEntity k JOIN k.projects p WHERE p.id = :projectId ORDER BY k.name", KeywordEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    public List<KeywordEntity> getKeywordsByFirstLetter(char firstLetter) {
        TypedQuery<KeywordEntity> query = em.createQuery(
                "SELECT k FROM KeywordEntity k WHERE k.name LIKE :pattern", KeywordEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }
}

