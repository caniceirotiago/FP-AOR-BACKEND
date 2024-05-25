package aor.fpbackend.bean;

import aor.fpbackend.dao.ConfigurationDao;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;

@Stateless
public class ConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    ConfigurationDao configurationDao;


    public void createDefaultConfigIfNotExistent(String configKey, int value) throws DatabaseOperationException {
        if (!configurationDao.checkConfigExist(configKey)) {
            ConfigurationEntity configEntity = new ConfigurationEntity(configKey, value);
            configurationDao.persist(configEntity);
        }
    }
    public int getConfigValueByKey(String configKey) {
        return configurationDao.findConfigValueByKey(configKey);
    }


    public void updateConfigValue(String configKey, int configValue) {
        ConfigurationEntity configEntity = configurationDao.findConfigEntityByKey(configKey);
        configEntity.setValue(configValue);
    }
}
