package aor.fpbackend.dto;

public class ProjectAskJoinDto {
    private long projectId;
    private long userId;

    public ProjectAskJoinDto(long projectId, long userId) {
        this.projectId = projectId;
        this.userId = userId;
    }
    public ProjectAskJoinDto() {
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
    //TODO validations
}
