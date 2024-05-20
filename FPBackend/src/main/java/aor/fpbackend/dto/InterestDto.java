package aor.fpbackend.dto;

public class InterestDto {
    private Long id;
    private String name;

    public InterestDto() {
    }

    public InterestDto(String name, Long id) {
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
