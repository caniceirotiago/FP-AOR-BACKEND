package aor.fpbackend.dao;

import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.KeywordEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
/**
 * GroupMessageDao class provides data access operations for {@link GroupMessageEntity}.
 * <p>
 * This class implements methods to perform CRUD operations and custom queries
 * related to group messages in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for group message management.
 * <br>
 */
@Stateless
public class GroupMessageDao extends AbstractDao<GroupMessageEntity> {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public GroupMessageDao() {
        super(GroupMessageEntity.class);
    }

    public List<GroupMessageEntity> getGroupMessagesByProjectId(long projectId) {
        TypedQuery<GroupMessageEntity> query = em.createQuery(
                "SELECT gm FROM GroupMessageEntity gm WHERE gm.group.id = :projectId ORDER BY gm.sentTime",
                GroupMessageEntity.class
        );
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    public GroupMessageEntity findGroupMessageById(long messageId) {
        try {
            return (GroupMessageEntity) em.createNamedQuery("GroupMessage.findGroupMessageById")
                    .setParameter("messageId", messageId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<GroupMessageEntity> findPreviousGroupMessages(long projectId, Instant sentTime) {
        try {
            TypedQuery<GroupMessageEntity> query = em.createNamedQuery("GroupMessage.findPreviousGroupMessages", GroupMessageEntity.class)
                    .setParameter("projectId", projectId)
                    .setParameter("sentTime", sentTime);

            return query.getResultList();
        } catch (NoResultException e) {
            return List.of(); // Using List.of() to return an immutable empty list
        }
    }

    public List<GroupMessageEntity> getMessagesByIds(List<Long> messageIds) {
        return em.createQuery("SELECT gm FROM GroupMessageEntity gm WHERE gm.id IN :messageIds", GroupMessageEntity.class)
                .setParameter("messageIds", messageIds)
                .getResultList();
    }
}
