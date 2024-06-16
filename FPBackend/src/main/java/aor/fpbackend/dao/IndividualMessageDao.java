package aor.fpbackend.dao;

import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class IndividualMessageDao extends AbstractDao<IndividualMessageEntity> {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public IndividualMessageDao() {
        super(IndividualMessageEntity.class);
    }
    public List<IndividualMessageEntity>    getIndividualMessages(String senderId, String recipientId){
        return em.createQuery("SELECT m FROM IndividualMessageEntity m WHERE (m.sender.id = :senderId AND m.recipient.id = :recipientId) OR (m.sender.id = :recipientId AND m.recipient.id = :senderId)", IndividualMessageEntity.class)
                .setParameter("senderId", Long.parseLong(senderId))
                .setParameter("recipientId", Long.parseLong(recipientId))
                .getResultList();
    }
    public List<IndividualMessageEntity> getReceivedMessages(String userId) {
        return em.createQuery("SELECT m FROM IndividualMessageEntity m WHERE m.recipient.id = :userId", IndividualMessageEntity.class)
                .setParameter("userId", Long.parseLong(userId))
                .getResultList();
    }
    public List<IndividualMessageEntity> getSentMessages(String userId) {
        return em.createQuery("SELECT m FROM IndividualMessageEntity m WHERE m.sender.id = :userId", IndividualMessageEntity.class)
                .setParameter("userId", Long.parseLong(userId))
                .getResultList();
    }

}
