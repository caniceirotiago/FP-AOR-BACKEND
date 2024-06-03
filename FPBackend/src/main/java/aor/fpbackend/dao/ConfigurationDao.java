package aor.fpbackend.dao;


import aor.fpbackend.dto.ConfigurationGetDto;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import aor.fpbackend.entity.ConfigurationEntity;

import java.util.List;


@Stateless
public class ConfigurationDao extends AbstractDao<ConfigurationEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    public ConfigurationDao() {
        super(ConfigurationEntity.class);
    }


    public boolean checkConfigExist(String configKey) {
        try {
            Long count = (Long) em.createNamedQuery("Configuration.countConfigByConfigKey")
                    .setParameter("configKey", configKey)
                    .getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    public int findConfigValueByKey(String configKey) {
        try {
            return (Integer) em.createNamedQuery("Configuration.findConfigValueByConfigKey")
                    .setParameter("configKey", configKey)
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    public ConfigurationEntity findConfigEntityByKey(String configKey) {
        try {
            return (ConfigurationEntity) em.createNamedQuery("Configuration.findConfigEntityByConfigKey")
                    .setParameter("configKey", configKey)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<ConfigurationGetDto> getAllConfiguration() {
        return em.createNamedQuery("Configuration.getAllConfiguration")
                .getResultList();
    }


}
