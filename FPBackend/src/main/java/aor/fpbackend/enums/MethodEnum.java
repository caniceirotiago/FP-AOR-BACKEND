package aor.fpbackend.enums;

public enum MethodEnum {
    UPDATE_ROLE(1L),
    ADD_SKILL_USER(2L),
    ADD_SKILL_PROJECT(3L),
    ALL_SKILLS(4L),
    SKILLS_BY_USER(5L),
    SKILLS_FIRST_LETTER(6L),
    REMOVE_SKILL_USER(7L),
    ADD_INTEREST(8L),
    ALL_INTERESTS(9L),
    INTERESTS_BY_USER(10L),
    INTERESTS_FIRST_LETTER(11L),
    REMOVE_INTEREST(12L),
    ADD_KEYWORD(13L),
    ALL_KEYWORDS(14L),
    KEYWORDS_BY_PROJECT(15L),
    KEYWORDS_FIRST_LETTER(16L),
    REMOVE_KEYWORD(17L),
    REMOVE_SKILL_PROJECT(18L),
    SKILLS_BY_PROJECT(19L),
    ADD_PROJECT(20L),
    ALL_PROJECTS(21L),
    PROJECT_BY_ID(22L),
    ADD_ASSET(23L),
    REMOVE_ASSET(24L),
    ALL_ASSETS(25L),
    ASSETS_FIRST_LETTER(26L),
    ASSETS_BY_PROJECT(27L),
    ADD_USER(28L),
    REMOVE_USER(29L),
    ADD_TASK(30L),
    ALL_TASKS (31L),
    TASKS_BY_PROJECT (32L);

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
