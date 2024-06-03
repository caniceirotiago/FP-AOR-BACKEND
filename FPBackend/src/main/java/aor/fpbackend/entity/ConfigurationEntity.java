package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * The ConfigurationEntity class is used to store key-value pairs for system settings.
 * The design is intended to provide a flexible and efficient means
 * of managing system configurations that are critical for application performance
 * and user management. Example usages include session timeout settings, maximum
 * login attempts, and resource limit configurations.
 */

@Entity
@Table(name = "configuration")

@NamedQuery(name = "Configuration.findConfigValueByConfigKey", query = "SELECT c.value FROM ConfigurationEntity c WHERE c.configKey = :configKey")
@NamedQuery(name = "Configuration.findConfigEntityByConfigKey", query = "SELECT c FROM ConfigurationEntity c WHERE c.configKey = :configKey")
@NamedQuery(name = "Configuration.countConfigByConfigKey", query = "SELECT count(c) FROM ConfigurationEntity c WHERE c.configKey = :configKey")
@NamedQuery(name = "Configuration.getAllConfiguration", query = "SELECT new aor.fpbackend.dto.ConfigurationGetDto(c.configKey, c.value) FROM ConfigurationEntity c")

public class ConfigurationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private int value;

    // Construtores
    public ConfigurationEntity() {
    }

    public ConfigurationEntity(String configKey, int value) {
        this.configKey = configKey;
        this.value = value;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String key) {
        this.configKey = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
