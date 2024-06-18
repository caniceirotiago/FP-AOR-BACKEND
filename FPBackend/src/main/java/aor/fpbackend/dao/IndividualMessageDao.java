package aor.fpbackend.dao;

import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Stateless
public class IndividualMessageDao extends AbstractDao<IndividualMessageEntity> {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public IndividualMessageDao() {
        super(IndividualMessageEntity.class);
    }
    public List<IndividualMessageEntity>    getIndividualMessages(String senderId, String recipientId){
        return em.createQuery("SELECT m FROM IndividualMessageEntity m WHERE (m.sender.id = :senderId AND m.recipient.id = :recipientId) OR (m.sender.id = :recipientId AND m.recipient.id = :senderId) ORDER BY m.sentTime", IndividualMessageEntity.class)
                .setParameter("senderId", Long.parseLong(senderId))
                .setParameter("recipientId", Long.parseLong(recipientId))
                .getResultList();
    }
    public List<IndividualMessageEntity> findSentMessages(String userId, int page, int pageSize, UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<IndividualMessageEntity> query = cb.createQuery(IndividualMessageEntity.class);
        Root<IndividualMessageEntity> messageRoot = query.from(IndividualMessageEntity.class);

        List<Predicate> predicates = createPredicates(uriInfo, cb, messageRoot);
        predicates.add(cb.equal(messageRoot.get("sender").get("id"), userId));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(messageRoot.get("sentTime")));

        TypedQuery<IndividualMessageEntity> typedQuery = em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);

        return typedQuery.getResultList();
    }

    public long countSentMessages(String userId, UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<IndividualMessageEntity> messageRoot = query.from(IndividualMessageEntity.class);

        query.select(cb.count(messageRoot));
        List<Predicate> predicates = createPredicates(uriInfo, cb, messageRoot);
        predicates.add(cb.equal(messageRoot.get("sender").get("id"), userId));

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(query).getSingleResult();
    }

    public List<IndividualMessageEntity> findReceivedMessages(String userId, int page, int pageSize, UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<IndividualMessageEntity> query = cb.createQuery(IndividualMessageEntity.class);
        Root<IndividualMessageEntity> messageRoot = query.from(IndividualMessageEntity.class);

        List<Predicate> predicates = createPredicates(uriInfo, cb, messageRoot);
        predicates.add(cb.equal(messageRoot.get("recipient").get("id"), userId));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(messageRoot.get("sentTime")));

        TypedQuery<IndividualMessageEntity> typedQuery = em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);

        return typedQuery.getResultList();
    }

    public long countReceivedMessages(String userId, UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<IndividualMessageEntity> messageRoot = query.from(IndividualMessageEntity.class);

        query.select(cb.count(messageRoot));
        List<Predicate> predicates = createPredicates(uriInfo, cb, messageRoot);
        predicates.add(cb.equal(messageRoot.get("recipient").get("id"), userId));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return em.createQuery(query).getSingleResult();
    }

    private List<Predicate> createPredicates(UriInfo uriInfo, CriteriaBuilder cb, Root<IndividualMessageEntity> messageRoot) {
        List<Predicate> predicates = new ArrayList<>();
        Map<String, List<String>> filters = uriInfo.getQueryParameters();


        filters.forEach((key, values) -> {
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                return;
            }
            switch (key) {
                case "search":
                    List<Predicate> orPredicates = new ArrayList<>();
                    orPredicates.add(cb.like(cb.lower(messageRoot.get("subject")), "%" + values.get(0).toLowerCase() + "%"));
                    orPredicates.add(cb.like(cb.lower(messageRoot.get("content")), "%" + values.get(0).toLowerCase() + "%"));
                    orPredicates.add(cb.like(cb.lower(messageRoot.get("sender").get("username")), "%" + values.get(0).toLowerCase() + "%"));
                    orPredicates.add(cb.like(cb.lower(messageRoot.get("recipient").get("username")), "%" + values.get(0).toLowerCase() + "%"));

                    // Combine all OR predicates into a single predicate
                    predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));

            }
        });

        return predicates;
    }

    public boolean markMessagesAsRead (List<Long> messageIds) {
        return em.createQuery("UPDATE IndividualMessageEntity m SET m.isViewed = true WHERE m.id IN :messageIds")
                .setParameter("messageIds", messageIds)
                .executeUpdate() > 0;
    }
    public List<IndividualMessageEntity> getMessagesByIds(List<Long> messageIds) {
        return em.createQuery("SELECT m FROM IndividualMessageEntity m WHERE m.id IN :messageIds", IndividualMessageEntity.class)
                .setParameter("messageIds", messageIds)
                .getResultList();
    }
}
