package aor.fpbackend.enums;

public enum LocationEnum {
    LISBOA("Lisboa"),
    COIMBRA("Coimbra"),
    PORTO("Porto"),
    TOMAR("Tomar"),
    VISEU("Viseu"),
    VILA_REAL("Vila Real");

    private final String value;

    LocationEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}