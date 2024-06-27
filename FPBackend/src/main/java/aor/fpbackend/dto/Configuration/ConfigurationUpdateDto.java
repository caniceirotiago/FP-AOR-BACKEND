package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class ConfigurationUpdateDto implements Serializable {
    @XmlElement
    @NotNull
    private String configKey;
    @XmlElement
    @NotNull
    private int configValue;

    public ConfigurationUpdateDto() {
    }

    public ConfigurationUpdateDto(String configKey, int configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public int getConfigValue() {
        return configValue;
    }

    public void setConfigValue(int configValue) {
        this.configValue = configValue;
    }
}
