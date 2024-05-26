package aor.fpbackend.enums;

public enum MethodEnum {
    UPDATE_ROLE(1L),
    ADD_SKILL(2L),
    ALL_SKILLS(3L),
    SKILL_BY_USER(4L),
    SKILL_FIRST_LETTER(5L),
    REMOVE_SKILL(6L),
    ADD_INTEREST(7L),
    ALL_INTERESTS(8L),
    INTEREST_BY_USER(9L),
    INTEREST_FIRST_LETTER(10L),
    REMOVE_INTEREST(11L),
    ADD_KEYWORD(12L),
    ALL_KEYWORDS(13L),
    KEYWORD_BY_PROJECT(14L),
    KEYWORD_FIRST_LETTER(15L),
    REMOVE_KEYWORD(16L);

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
