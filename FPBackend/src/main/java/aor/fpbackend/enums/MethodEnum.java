package aor.fpbackend.enums;

public enum MethodEnum {
    UPDATE_ROLE(1L),
    ADD_SKILL(2L),
    ALL_SKILLS(3L),
    SKILL_BY_USER(4L),
    SKILL_FIRST_LETTER(5L);

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
