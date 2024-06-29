package aor.fpbackend.enums;

public enum AssetTypeEnum {
    COMPONENT ("Component"),
    RESOURCE("Resource");

    private final String value;

    AssetTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    }