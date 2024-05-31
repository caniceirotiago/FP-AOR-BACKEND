package aor.fpbackend.dto;

import java.util.List;

public class ProjectsPaginatedDto {
    private List<ProjectGetDto> projectsForAPage;
    private long totalProjects;

    public ProjectsPaginatedDto(List<ProjectGetDto> projectsForAPage, long totalProjects) {
        this.projectsForAPage = projectsForAPage;
        this.totalProjects = totalProjects;
    }

    public List<ProjectGetDto> getProjectsForAPage() {
        return projectsForAPage;
    }

    public void setProjectsForAPage(List<ProjectGetDto> projectsForAPage) {
        this.projectsForAPage = projectsForAPage;
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }
}
