package aor.fpbackend.dao;

import aor.fpbackend.entity.*;
import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.HashMap;
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> query = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> projectRoot = query.from(ProjectEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        Map<String, List<String>> filters = uriInfo.getQueryParameters()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("pageSize") && !entry.getKey().equals("sortBy"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        filters.forEach((key, values) -> {
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                return;
            }
            switch (key) {
                case "name":
                    predicates.add(cb.like(cb.lower(projectRoot.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case "state":
                    try {
                        ProjectStateEnum state = ProjectStateEnum.valueOf(values.get(0).toUpperCase());
                        predicates.add(cb.equal(projectRoot.get("state"), state));
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid value for project state: " + values.get(0));
                    }
                    break;
                case "keywords":
                    Join<ProjectEntity, KeywordEntity> keywordJoin = projectRoot.join("projectKeywords");
                    predicates.add(cb.like(cb.lower(keywordJoin.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case "skills":
                    Join<ProjectEntity, SkillEntity> skillJoin = projectRoot.join("projectSkills");
                    predicates.add(cb.like(cb.lower(skillJoin.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                // Add more filters as needed
            }
        });

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Adding logic for sorting
        String sortBy = uriInfo.getQueryParameters().getFirst("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "creationDate":
                    query.orderBy(cb.asc(projectRoot.get("creationDate")));
                    break;
                case "openPositions":
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ProjectMembershipEntity> subRoot = subquery.from(ProjectMembershipEntity.class);
                    subquery.select(cb.count(subRoot.get("id"))).where(cb.equal(subRoot.get("project"), projectRoot));
                    Expression<Integer> maxMembersExpr = cb.literal(getMaxProjectMembers());
                    Expression<Long> currentMembersExpr = cb.coalesce(subquery.getSelection(), 0L);
                    Expression<Integer> openPositionsExpr = cb.diff(maxMembersExpr, currentMembersExpr.as(Integer.class));
                    query.orderBy(cb.asc(openPositionsExpr));
                    break;
                case "state":
                    query.orderBy(cb.asc(projectRoot.get("state")));
                    break;
                default:
                    // Default sorting, if any
                    break;
            }
        }

        TypedQuery<ProjectEntity> typedQuery = em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);

        return typedQuery.getResultList();
    }


    private int getMaxProjectMembers() {
        String configKey = "maxProjectMembers";
        TypedQuery<Integer> query = em.createNamedQuery("Configuration.findConfigValueByConfigKey", Integer.class);
        query.setParameter("configKey", configKey);
        return query.getSingleResult();
    }





    public long countFilteredProjects(UriInfo uriInfo) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(p) FROM ProjectEntity p WHERE 1=1");

        if (uriInfo.getQueryParameters().containsKey("projectSkills")) {
            queryString.append(" AND p.projectSkills IN :projectSkills");
            parameters.put("projectSkills", uriInfo.getQueryParameters().get("projectSkills"));
        }

        if (uriInfo.getQueryParameters().containsKey("name")) {
            queryString.append(" AND p.name LIKE :name");
            parameters.put("name", "%" + uriInfo.getQueryParameters().getFirst("name") + "%");
        }

        if (uriInfo.getQueryParameters().containsKey("state")) {
            try {
                List<String> stateStrings = uriInfo.getQueryParameters().get("state");
                List<ProjectStateEnum> states = stateStrings.stream()
                        .map(state -> ProjectStateEnum.valueOf(state.toUpperCase()))
                        .collect(Collectors.toList());
                queryString.append(" AND p.state IN :state");
                parameters.put("state", states);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid value for project state: " + uriInfo.getQueryParameters().get("state"));
            }
        }

        if (uriInfo.getQueryParameters().containsKey("projectKeywords")) {
            queryString.append(" AND p.projectKeywords IN :projectKeywords");
            parameters.put("projectKeywords", uriInfo.getQueryParameters().get("projectKeywords"));
        }

        Query query = em.createQuery(queryString.toString());
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return (long) query.getSingleResult();
    }


}
