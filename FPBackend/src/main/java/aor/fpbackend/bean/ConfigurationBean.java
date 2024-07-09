package aor.fpbackend.bean;

import aor.fpbackend.dao.ConfigurationDao;
import aor.fpbackend.dto.Configuration.ConfigurationGetDto;
import aor.fpbackend.dto.Configuration.ConfigurationUpdateDto;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.util.List;

@Stateless
public class ConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    ConfigurationDao configurationDao;

    /**
     * Creates a default configuration entry if it does not already exist in the database.
     *
     * @param configKey The key of the configuration entry.
     * @param value     The value associated with the configuration entry.
     */
    public void createDefaultConfigIfNotExistent(String configKey, int value) {
        if (!configurationDao.checkConfigExist(configKey)) {
            ConfigurationEntity configEntity = new ConfigurationEntity(configKey, value);
            configurationDao.persist(configEntity);
        }
    }

    /**
     * Retrieves the value of a configuration entry based on the given key.
     *
     * @param configKey The key of the configuration entry whose value is to be retrieved.
     * @return The value associated with the specified configuration key.
     * @throws EntityNotFoundException If no configuration entry with the specified key is found.
     */
    public int getConfigValueByKey(String configKey) {
        return configurationDao.findConfigValueByKey(configKey);
    }

    /**
     * Retrieves all configurations from the database.
     *
     * @return A list of ConfigurationGetDto objects representing all configurations.
     */
    public List<ConfigurationGetDto> getAllConfiguration() {
        return configurationDao.getAllConfiguration();
    }

    /**
     * Updates the value of a configuration entry based on the provided ConfigurationUpdateDto.
     *
     * @param configUpdateDto The DTO containing the updated configuration key and value.
     * @throws InputValidationException If the new configuration value does not meet validation criteria,
     *                                  such as being lower than the minimum session timeout value.
     * @throws EntityNotFoundException  If no configuration entry with the specified key is found.
     */
    public void updateConfigValue(ConfigurationUpdateDto configUpdateDto) throws InputValidationException, EntityNotFoundException {
        // Retrieve the configuration entity based on the provided configuration key
        ConfigurationEntity configEntity = configurationDao.findConfigEntityByKey(configUpdateDto.getConfigKey());
        // Check if a configuration entity with the specified key exists
        if (configEntity == null) {
            throw new EntityNotFoundException("Configuration not found");
        }
        // If the configuration key is "sessionTimeout", perform additional validation
        if (configUpdateDto.getConfigKey().equals("sessionTimeout")) {
            // new value cannot be lower than the minimum allowed value (10 minutes)
            if (configUpdateDto.getConfigValue() < 10) {
                throw new InputValidationException("New session timeout value lower than the minimum allowed value");
            }
        }
        configEntity.setValue(configUpdateDto.getConfigValue());
    }

}
