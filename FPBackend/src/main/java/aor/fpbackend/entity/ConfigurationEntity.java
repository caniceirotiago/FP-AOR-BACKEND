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
public class ConfigurationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(name = "config_value", nullable = false)
    private int value;

    // Construtores
    public ConfigurationEntity() {}

    public ConfigurationEntity(String key, int value) {
        this.key = key;
        this.value = value;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
