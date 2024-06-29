package aor.fpbackend.enums;

public enum AssetTypeEnum {
    COMPONENT ("COMPONENT"),
    RESOURCE("RESOURCE");

    private final String value;

    AssetTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    }