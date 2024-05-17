package aor.fpbackend.bean;

import aor.fpbackend.dao.ConfigurationDao;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;

import java.io.Serializable;

public class ConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    ConfigurationDao configurationDao;

    public void initializeDefaultConfigurations() throws DatabaseOperationException {
        String sessionTimeoutKey = "sessionTimeout";
        if (!configurationDao.checkConfigExist(sessionTimeoutKey)) {
            ConfigurationEntity sessionTimeout = new ConfigurationEntity(sessionTimeoutKey, 1800); //Session timeout in seconds
            configurationDao.persist(sessionTimeout);
        }
    }

    public void updateConfigValue(String configKey, int configValue) {
        ConfigurationEntity configEntity = configurationDao.findConfigEntityByKey(configKey);
        configEntity.setValue(configValue);
    }
}
