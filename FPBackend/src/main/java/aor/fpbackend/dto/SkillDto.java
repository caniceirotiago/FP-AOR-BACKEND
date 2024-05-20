package aor.fpbackend.dto;

public class SkillDto {
    private Long id;
    private String name;

    public SkillDto() {
    }

    public SkillDto(String name, Long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
