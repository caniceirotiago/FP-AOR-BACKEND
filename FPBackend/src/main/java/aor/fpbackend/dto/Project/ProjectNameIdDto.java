package aor.fpbackend.dto.Project;

import aor.fpbackend.enums.ProjectStateEnum;

import java.time.Instant;

public class ProjectNameIdDto {
    private long id;
    private String name;
    private Instant createdAt;
    private Enum<ProjectStateEnum> status;

    public ProjectNameIdDto() {
    }

    public ProjectNameIdDto(long id, String name, Instant createdAt, Enum<ProjectStateEnum> status) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Enum<ProjectStateEnum> getStatus() {
        return status;
    }

    public void setStatus(Enum<ProjectStateEnum> status) {
        this.status = status;
    }
}
