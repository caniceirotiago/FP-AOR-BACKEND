package aor.fpbackend.enums;

public enum MethodEnum {
    UPDATE_ROLE(1L),
    ADD_SKILL_USER(2L),
    ADD_SKILL_PROJECT(3L),
    ALL_SKILLS(4L),
    SKILL_BY_USER(5L),
    SKILL_FIRST_LETTER(6L),
    REMOVE_SKILL(7L),
    ADD_INTEREST(8L),
    ALL_INTERESTS(9L),
    INTEREST_BY_USER(10L),
    INTEREST_FIRST_LETTER(11L),
    REMOVE_INTEREST(12L),
    ADD_KEYWORD(13L),
    ALL_KEYWORDS(14L),
    KEYWORD_BY_PROJECT(15L),
    KEYWORD_FIRST_LETTER(16L),
    REMOVE_KEYWORD(17L);

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
