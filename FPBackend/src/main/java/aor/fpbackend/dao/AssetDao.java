package aor.fpbackend.dao;

import aor.fpbackend.entity.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class AssetDao extends AbstractDao<AssetEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public AssetDao() {
        super(AssetEntity.class);
    }

    public boolean checkAssetExistByName(String name) {
        try {
            Long count = (Long) em.createNamedQuery("Asset.countAssetByName")
                    .setParameter("name", name)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean checkAssetExistByPartNumber(String partNumber) {
        try {
            Long count = (Long) em.createNamedQuery("Asset.countAssetByPartNumber")
                    .setParameter("partNumber", partNumber)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public AssetEntity findAssetByName(String name) {
        try {
            return (AssetEntity) em.createNamedQuery("Asset.findAssetByName")
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AssetEntity findAssetById(long assetId) {
        try {
            return (AssetEntity) em.createNamedQuery("Asset.findAssetById")
                    .setParameter("assetId", assetId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public List<AssetEntity> getAllAssets() {
        TypedQuery<AssetEntity> query = em.createQuery("SELECT a FROM AssetEntity a", AssetEntity.class);
        return query.getResultList();
    }

    public List<AssetEntity> getAssetsByProjectId(long projectId) {
        TypedQuery<AssetEntity> query = em.createQuery("SELECT a FROM AssetEntity a JOIN a.projectAssets p WHERE p.id = :projectId ORDER BY a.name", AssetEntity.class);
        query.setParameter("projectId", projectId);
        return query.getResultList();
    }

    public List<AssetEntity> getAssetsByFirstLetter(String firstLetter) {
        TypedQuery<AssetEntity> query = em.createQuery(
                "SELECT a FROM AssetEntity a WHERE LOWER(a.name) LIKE :pattern", AssetEntity.class);
        query.setParameter("pattern", firstLetter + "%");
        return query.getResultList();
    }

    public List<AssetEntity> findFilteredAssets(int page, int pageSize, UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AssetEntity> query = cb.createQuery(AssetEntity.class);
        Root<AssetEntity> assetRoot = query.from(AssetEntity.class);
        // Create predicates based on query parameters
        List<Predicate> predicates = createPredicates(uriInfo, cb, assetRoot);
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        // Adding logic for sorting
        String orderBy = uriInfo.getQueryParameters().getFirst("orderBy");
        String sortBy = uriInfo.getQueryParameters().getFirst("sortBy");
        // Apply sorting if 'sortBy' parameter is provided
        if (sortBy != null && !sortBy.isEmpty()) {
            Order order;
            if ("desc".equalsIgnoreCase(orderBy)) {
                order = cb.desc(assetRoot.get(sortBy));
            } else {
                order = cb.asc(assetRoot.get(sortBy));
            }
            query.orderBy(order);
        }
        // Create a typed query with pagination
        TypedQuery<AssetEntity> typedQuery = em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);
        return typedQuery.getResultList();
    }

    public long countFilteredAssets(UriInfo uriInfo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AssetEntity> assetRoot = query.from(AssetEntity.class);
        query.select(cb.count(assetRoot));
        List<Predicate> predicates = createPredicates(uriInfo, cb, assetRoot);
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return em.createQuery(query).getSingleResult();
    }

    private List<Predicate> createPredicates(UriInfo uriInfo, CriteriaBuilder cb, Root<AssetEntity> assetRoot) {
        List<Predicate> predicates = new ArrayList<>();

        Map<String, List<String>> filters = uriInfo.getQueryParameters()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("pageSize") && !entry.getKey().equals("orderBy") && !entry.getKey().equals("sortBy"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        filters.forEach((key, values) -> {
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                return;
            }
            switch (key) {
                case "name":
                    predicates.add(cb.like(cb.lower(assetRoot.get("name")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case "type":
                    try {
                        AssetTypeEnum type = AssetTypeEnum.valueOf(values.get(0).toUpperCase());
                        predicates.add(cb.equal(assetRoot.get("type"), type));
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid value for asset type: " + values.get(0));
                    }
                    break;
                case "manufacturer":
                    predicates.add(cb.like(cb.lower(assetRoot.get("manufacturer")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
                case "partNumber":
                    predicates.add(cb.like(cb.lower(assetRoot.get("partNumber")), "%" + values.get(0).toLowerCase() + "%"));
                    break;
            }
        });
        return predicates;
    }

}
