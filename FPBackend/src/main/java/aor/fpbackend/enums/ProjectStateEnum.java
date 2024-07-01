package aor.fpbackend.enums;

public enum ProjectStateEnum {
    PLANNING("Planning"),
    READY("Ready"),
    IN_PROGRESS("In Progress"),
    FINISHED("Finished"),
    CANCELLED("Cancelled");

    private final String value;

    ProjectStateEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
