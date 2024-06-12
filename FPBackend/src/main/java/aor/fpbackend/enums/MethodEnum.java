package aor.fpbackend.enums;

public enum MethodEnum {
    ADMIN_LEVEL_USERS(1L),
    STANDARD_LEVEL_USERS(2L),
    ADMIN_LEVEL_SKILLS(3L),
    STANDARD_LEVEL_SKILLS(4L),
    ADMIN_LEVEL_INTERESTS(5L),
    STANDARD_LEVEL_INTERESTS(6L),
    ADMIN_LEVEL_KEYWORDS(7L),
    STANDARD_LEVEL_KEYWORDS(8L),
    ADMIN_LEVEL_PROJECTS(9L),
    STANDARD_LEVEL_PROJECTS(10L),
    ADMIN_LEVEL_ASSETS(11L),
    STANDARD_LEVEL_ASSETS(12L),
    ADMIN_LEVEL_TASKS(13L),
    STANDARD_LEVEL_TASKS(14L),
    ADMIN_LEVEL_MEMBERSHIPS(15L),
    STANDARD_LEVEL_MEMBERSHIPS(16L);

    private final long value;

    // Constructor
    MethodEnum(long value) {
        this.value = value;
    }

    // Getter
    public long getValue() {
        return value;
    }
}
