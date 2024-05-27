package aor.fpbackend.dao;

import aor.fpbackend.entity.AssetEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

    @Stateless
    public class AssetDao extends AbstractDao<AssetEntity> {
        private static final long serialVersionUID = 1L;

        @PersistenceContext
        private EntityManager em;

        public AssetDao() {
            super(AssetEntity.class);
        }

        public boolean checkAssetExist(String name) {
            try {
                Long count = (Long) em.createNamedQuery("Asset.countAssetByName")
                        .setParameter("name", name)
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

        public AssetEntity findKeywordById(long assetId) {
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

        public List<AssetEntity> getAssetsByFirstLetter(char firstLetter) {
            TypedQuery<AssetEntity> query = em.createQuery(
                    "SELECT a FROM AssetEntity a WHERE a.name LIKE :pattern", AssetEntity.class);
            query.setParameter("pattern", firstLetter + "%");
            return query.getResultList();
        }
    }
