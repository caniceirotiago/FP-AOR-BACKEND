package aor.fpbackend.bean;

import aor.fpbackend.dao.ConfigurationDao;
import aor.fpbackend.dto.ConfigurationGetDto;
import aor.fpbackend.dto.ConfigurationUpdateDto;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.exception.InputValidationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.List;

@Stateless
public class ConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ConfigurationBean.class);

    @EJB
    ConfigurationDao configurationDao;


    public void createDefaultConfigIfNotExistent(String configKey, int value) {
        if (!configurationDao.checkConfigExist(configKey)) {
            ConfigurationEntity configEntity = new ConfigurationEntity(configKey, value);
            configurationDao.persist(configEntity);
        }
    }

    public int getConfigValueByKey(String configKey) {
        return configurationDao.findConfigValueByKey(configKey);
    }

    public void updateConfigValue(ConfigurationUpdateDto configUpdateDto) throws InputValidationException {
        ConfigurationEntity configEntity = configurationDao.findConfigEntityByKey(configUpdateDto.getConfigKey());
        if(configUpdateDto.getConfigKey().equals("sessionTimeout")){
            if (configUpdateDto.getConfigValue()<10){
                throw new InputValidationException("New config value lower than minimum session timeout value");
            }
        }
        configEntity.setValue(configUpdateDto.getConfigValue());
    }

    public List<ConfigurationGetDto> getAllConfiguration() {
        return configurationDao.getAllConfiguration();
    }
}
