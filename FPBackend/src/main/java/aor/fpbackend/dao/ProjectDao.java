package aor.fpbackend.dao;

import aor.fpbackend.entity.*;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.QueryParams;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProjectDao class provides data access operations for {@link ProjectEntity}.
 * <p>
 * This class implements methods to perform CRUD operations and custom queries
 * related to projects in the database. It extends the {@link AbstractDao} class to inherit
 * generic data access operations and adds specific methods for project management.
 * <br>
 */
@Stateless
public class ProjectDao extends AbstractDao<ProjectEntity> {
    private static final long serialVersionUID = 1L;

    public ProjectDao() {
        super(ProjectEntity.class);
    }

    @PersistenceContext
    private EntityManager em;

    @EJB
    ConfigurationDao configDao;


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
    public List<Long> getAllProjectsIds() {
        try {
            return (List<Long>) em.createNamedQuery("Project.getAllProjectsIds").getResultList();
        } catch (NoResultException e) {
            return null;
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
        System.out.println("findFilteredProjects");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> query = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> projectRoot = query.from(ProjectEntity.class);

        List<Predicate> predicates = createPredicates(uriInfo, cb, projectRoot);

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Adding logic for sorting
        String orderBy = uriInfo.getQueryParameters().getFirst(QueryParams.ORDER_BY);
        String sortBy = uriInfo.getQueryParameters().getFirst(QueryParams.SORT_BY);
        System.out.println(uriInfo.getQueryParameters());

        List<Order> orderList = new ArrayList<>();

        // Apply sorting if 'sortBy' parameter is provided
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case QueryParams.CREATION_DATE:
                    orderList.add(QueryParams.DESC.equalsIgnoreCase(orderBy) ? cb.desc(projectRoot.get(QueryParams.CREATION_DATE)) : cb.asc(projectRoot.get(QueryParams.CREATION_DATE)));
                    break;
                case QueryParams.OPEN_POSITIONS:
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ProjectMembershipEntity> subRoot = subquery.from(ProjectMembershipEntity.class);
                    subquery.select(cb.count(subRoot.get("id"))).where(cb.equal(subRoot.get("project"), projectRoot));
                    Expression<Integer> maxMembersExpr = cb.literal(configDao.getMaxProjectMembers());
                    Expression<Long> currentMembersExpr = cb.coalesce(subquery.getSelection(), 0L);
                    Expression<Integer> openPositionsExpr = cb.diff(maxMembersExpr, currentMembersExpr.as(Integer.class));
                    orderList.add(QueryParams.DESC.equalsIgnoreCase(orderBy) ? cb.desc(openPositionsExpr) : cb.asc(openPositionsExpr));
                    break;
                case QueryParams.STATE:
                    orderList.add(QueryParams.DESC.equalsIgnoreCase(orderBy) ? cb.desc(projectRoot.get(QueryParams.STATE)) : cb.asc(projectRoot.get(QueryParams.STATE)));
                    break;
                default:
                    // Invalid 'sortBy' parameter, use default sorting
                    orderList.add(cb.asc(projectRoot.get(QueryParams.STATE)));
                    break;
            }
        } else {
            // Default sorting if 'sortBy' parameter is not provided
            orderList.add(cb.desc(projectRoot.get(QueryParams.STATE)));
            orderList.add(cb.desc(projectRoot.get(QueryParams.CREATION_DATE)));
        }


        query.orderBy(orderList);

        TypedQuery<ProjectEntity> typedQuery = em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);

        return typedQuery.getResultList();
    }


    public long countFilteredProjects(UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProjectEntity> projectRoot = query.from(ProjectEntity.class);

        query.select(cb.count(projectRoot));

        List<Predicate> predicates = createPredicates(uriInfo, cb, projectRoot);

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(query).getSingleResult();
    }

    private List<Predicate> createPredicates(UriInfo uriInfo, CriteriaBuilder cb, Root<ProjectEntity> projectRoot) {
        List<Predicate> predicates = new ArrayList<>();
        Map<String, List<String>> filters = uriInfo.getQueryParameters()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals(QueryParams.PAGE) &&
                        !entry.getKey().equals(QueryParams.PAGE_SIZE) &&
                        !entry.getKey().equals(QueryParams.ORDER_BY) &&
                        !entry.getKey().equals(QueryParams.SORT_BY))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        filters.forEach((key, values) -> {
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                return;
            }
            switch (key) {
                case QueryParams.NAME:
                    predicates.add(cb.like(cb.lower(projectRoot.get(QueryParams.NAME)), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case QueryParams.STATE:
                    try {
                        ProjectStateEnum state = ProjectStateEnum.valueOf(values.get(0).toUpperCase());
                        predicates.add(cb.equal(projectRoot.get(QueryParams.STATE), state));
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid value for project state: " + values.get(0));
                    }
                    break;
                case QueryParams.KEYWORDS:
                    Join<ProjectEntity, KeywordEntity> keywordJoin = projectRoot.join("projectKeywords");
                    predicates.add(cb.like(cb.lower(keywordJoin.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case QueryParams.SKILLS:
                    Join<ProjectEntity, SkillEntity> skillJoin = projectRoot.join("projectSkills");
                    predicates.add(cb.like(cb.lower(skillJoin.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case QueryParams.LABORATORY:
                    Join<ProjectEntity, LaboratoryEntity> labJoin = projectRoot.join("laboratory");
                    predicates.add(cb.equal(labJoin.get("id"), Long.parseLong(values.get(0))));
                    break;
                case QueryParams.SHOW_ONLY_OWN_PROJECTS:
                    Join<ProjectEntity, ProjectMembershipEntity> membershipJoin = projectRoot.join("members");
                    predicates.add(cb.and(
                            cb.equal(membershipJoin.get("user").get("id"), Long.parseLong(values.get(0))),
                            cb.isTrue(membershipJoin.get("isAccepted"))
                    ));
                    break;
            }
        });

        return predicates;
    }



    // Methods for Project Report
    public List<Object[]> countProjectsByLaboratory() {
        TypedQuery<Object[]> query = em.createNamedQuery("Project.countProjectsByLaboratory", Object[].class);
        return query.getResultList();
    }

    public Double getAverageMembersPerProject() {
        Query query = em.createNamedQuery("Project.averageMembersPerProject");
        return (Double) query.getSingleResult();
    }

    public List<Object[]> getProjectsByLocationAndApproval(boolean isApproved) {
        Query query = em.createNamedQuery("Project.projectsByLocationAndApproval");
        query.setParameter("isApprovedParam", isApproved);
        return query.getResultList();
    }

    public List<Object[]> getProjectsByLocationAndState(ProjectStateEnum state) {
        Query query = em.createNamedQuery("Project.projectsByLocationAndState");
        query.setParameter("stateParam", state);
        return query.getResultList();
    }

    public Double getAverageProjectDuration() {
        Query query = em.createNamedQuery("Project.averageProjectDuration");
        return (Double) query.getSingleResult();
    }



}
