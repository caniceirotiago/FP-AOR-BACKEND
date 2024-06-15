package aor.fpbackend.dao;

import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.KeywordEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class GroupMessageDao extends AbstractDao<GroupMessageEntity> {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public GroupMessageDao() {
        super(GroupMessageEntity.class);
    }



    public List<GroupMessageEntity> getGroupMessagesByProjectId(long projectId) {
        TypedQuery<GroupMessageEntity> query = em.createQuery("SELECT gm FROM GroupMessageEntity gm JOIN gm.group p " +
                "WHERE p = :projectId ORDER BY gm.sentTime", GroupMessageEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }
}
