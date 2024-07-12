package aor.fpbackend.dao;

import aor.fpbackend.entity.TaskEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
/**
 * TaskDao class provides data access operations for {@link TaskEntity}.
 * <br>
 * This class implements methods to perform CRUD operations and custom queries
 * related to tasks in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for task management.
 * <br>
 */
@Stateless
public class TaskDao extends AbstractDao<TaskEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public TaskDao() {
        super(TaskEntity.class);
    }


    public TaskEntity findTaskByTitle(String title) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskByTitle")
                    .setParameter("title", title)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public TaskEntity findTaskById(long taskId) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById")
                    .setParameter("taskId", taskId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> findAllTasks() {
        try {
            return (List<TaskEntity>) em.createNamedQuery("Task.findAll")
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> getTasksByProjectId(long projectId) {
        TypedQuery<TaskEntity> query = em.createQuery("SELECT t FROM TaskEntity t JOIN t.project p WHERE p.id = :projectId ORDER BY t.plannedStartDate", TaskEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    public boolean checkTitleExist(String title) {
        try {
            Long count = (Long) em.createNamedQuery("Task.countTaskByTitle")
                    .setParameter("title", title)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
}
