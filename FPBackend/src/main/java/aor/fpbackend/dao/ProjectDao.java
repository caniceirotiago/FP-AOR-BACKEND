package aor.fpbackend.dao;

import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Stateless
public class ProjectDao extends AbstractDao<ProjectEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectDao() {
        super(ProjectEntity.class);
    }

    @PersistenceContext
    private EntityManager em;


    public boolean checkProjectNameExist(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Project.countProjectByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean isProjectMember(long projectId, long userId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(m) FROM ProjectMembershipEntity m WHERE m.project.id = :projectId AND m.user.id = :userId", Long.class);
        query.setParameter("projectId", projectId);
        query.setParameter("userId", userId);
        Long count = query.getSingleResult();
        return count > 0;
    }

    public ProjectEntity findProjectById(long projectId) {
        try {
            return (ProjectEntity) em.createNamedQuery("Project.findProjectById")
                    .setParameter("projectId", projectId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProjectEntity findProjectByName(String name) {
        try {
            return (ProjectEntity) em.createNamedQuery("Project.findProjectByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ArrayList<ProjectEntity> findAllProjects() {
        try {
            return (ArrayList<ProjectEntity>) em.createNamedQuery("Project.findAllProjects").getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ProjectEntity> findFilteredProjects(int page, int pageSize, UriInfo uriInfo) {
        Map<String, List<String>> filters = uriInfo.getQueryParameters()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("pageSize"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        StringBuilder queryString = new StringBuilder("SELECT p FROM ProjectEntity p WHERE 1=1");

        filters.forEach((key, values) -> {
            queryString.append(" AND p.").append(key).append(" IN :").append(key);
        });

        TypedQuery<ProjectEntity> query = em.createQuery(queryString.toString(), ProjectEntity.class);

        filters.forEach((key, values) -> {
            query.setParameter(key, values);
        });

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public long countFilteredProjects(UriInfo uriInfo) {
        Map<String, List<String>> filters = uriInfo.getQueryParameters()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("pageSize"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        StringBuilder queryString = new StringBuilder("SELECT COUNT(p) FROM ProjectEntity p WHERE 1=1");

        filters.forEach((key, values) -> {
            queryString.append(" AND p.").append(key).append(" IN :").append(key);
        });

        TypedQuery<Long> query = em.createQuery(queryString.toString(), Long.class);

        filters.forEach((key, values) -> {
            query.setParameter(key, values);
        });

        return query.getSingleResult();
    }

}
