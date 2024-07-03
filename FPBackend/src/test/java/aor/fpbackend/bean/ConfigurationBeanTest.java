package aor.fpbackend.bean;

import aor.fpbackend.dao.ConfigurationDao;
import aor.fpbackend.dto.Configuration.ConfigurationGetDto;
import aor.fpbackend.dto.Configuration.ConfigurationUpdateDto;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationBeanTest {

    @InjectMocks
    private ConfigurationBean configurationBean;

    @Mock
    private ConfigurationDao configurationDao;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDefaultConfigIfNotExistent_ConfigDoesNotExist() {
        String configKey = "testKey";
        int value = 10;

        when(configurationDao.checkConfigExist(configKey)).thenReturn(false);

        configurationBean.createDefaultConfigIfNotExistent(configKey, value);

        verify(configurationDao, times(1)).persist(any(ConfigurationEntity.class));
    }

    @Test
    void testCreateDefaultConfigIfNotExistent_ConfigExists() {
        String configKey = "testKey";
        int value = 10;

        when(configurationDao.checkConfigExist(configKey)).thenReturn(true);

        configurationBean.createDefaultConfigIfNotExistent(configKey, value);

        verify(configurationDao, never()).persist(any(ConfigurationEntity.class));
    }

    @Test
    void testGetConfigValueByKey() {
        String configKey = "testKey";
        int expectedValue = 10;

        when(configurationDao.findConfigValueByKey(configKey)).thenReturn(expectedValue);

        int actualValue = configurationBean.getConfigValueByKey(configKey);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void testUpdateConfigValue_Success() throws InputValidationException, EntityNotFoundException {
        String configKey = "testKey";
        int newValue = 20;
        ConfigurationUpdateDto updateDto = new ConfigurationUpdateDto(configKey, newValue);
        ConfigurationEntity configEntity = new ConfigurationEntity(configKey, 10);

        when(configurationDao.findConfigEntityByKey(configKey)).thenReturn(configEntity);

        configurationBean.updateConfigValue(updateDto);

        assertEquals(newValue, configEntity.getValue());
    }

    @Test
    void testUpdateConfigValue_ConfigNotFound() {
        String configKey = "testKey";
        int newValue = 20;
        ConfigurationUpdateDto updateDto = new ConfigurationUpdateDto(configKey, newValue);

        when(configurationDao.findConfigEntityByKey(configKey)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> configurationBean.updateConfigValue(updateDto));
    }

    @Test
    void testUpdateConfigValue_InvalidSessionTimeout() {
        String configKey = "sessionTimeout";
        int newValue = 5;
        ConfigurationUpdateDto updateDto = new ConfigurationUpdateDto(configKey, newValue);
        ConfigurationEntity configEntity = new ConfigurationEntity(configKey, 10);

        when(configurationDao.findConfigEntityByKey(configKey)).thenReturn(configEntity);

        assertThrows(InputValidationException.class, () -> configurationBean.updateConfigValue(updateDto));
    }

    @Test
    void testGetAllConfiguration() {
        List<ConfigurationGetDto> expectedConfigurations = List.of(new ConfigurationGetDto("key1", 10), new ConfigurationGetDto("key2", 20));

        when(configurationDao.getAllConfiguration()).thenReturn(expectedConfigurations);

        List<ConfigurationGetDto> actualConfigurations = configurationBean.getAllConfiguration();

        assertEquals(expectedConfigurations, actualConfigurations);
    }
}
