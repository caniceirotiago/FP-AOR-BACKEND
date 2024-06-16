package aor.fpbackend.dao;

import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.IndividualMessageEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class IndividualMessageDao extends AbstractDao<IndividualMessageEntity> {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public IndividualMessageDao() {
        super(IndividualMessageEntity.class);
    }

    public IndividualMessageEntity findIndividualMessageById(long messageId) {
        try {
            return (IndividualMessageEntity) em.createNamedQuery("IndividualMessage.findIndividualMessageById")
                    .setParameter("messageId", messageId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
