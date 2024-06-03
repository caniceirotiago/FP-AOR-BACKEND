package aor.fpbackend.dto;

public class ConfigurationGetDto {
    private String configKey;
    private int configValue;

    public ConfigurationGetDto(String configKey, int configValue) {
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
