package aor.fpbackend.dao;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;

import java.io.Serializable;
import java.util.List;
/**
 * AbstractDao class provides a generic implementation of common database operations.
 * <p>
 * This abstract class defines standard CRUD operations (Create, Read, Update, Delete)
 * that can be extended by specific DAO implementations for different entity types.
 * The class is annotated with {@link TransactionAttribute} to specify the transaction
 * management behavior, ensuring that each method runs within a required transaction context.
 * <br>
 *
 * @param <T> the type of the entity for which this DAO is responsible.
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public abstract class AbstractDao<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<T> clazz;

    @PersistenceContext(unitName = "PersistenceUnit")
    protected EntityManager em;

    public AbstractDao(Class<T> clazz)
    {
        this.clazz = clazz;
    }


    public T find(Object id)
    {
        return em.find(clazz, id);
    }


    public void persist(final T entity)
    {
        em.persist(entity);
    }


    public void merge(final T entity)
    {
        em.merge(entity);
    }

    public void remove(final T entity)
    {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }


    public List<T> findAll()
    {
        final CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(clazz);
        criteriaQuery.select(criteriaQuery.from(clazz));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public void deleteAll()
    {
        final CriteriaDelete<T> criteriaDelete = em.getCriteriaBuilder().createCriteriaDelete(clazz);
        criteriaDelete.from(clazz);
        em.createQuery(criteriaDelete).executeUpdate();
    }

    public void flush() {
        em.flush();
    }
}
