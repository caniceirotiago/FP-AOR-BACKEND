package aor.fpbackend.dto.Project;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class ProjectPaginatedDto implements Serializable {
    @XmlElement
    private List<ProjectGetDto> projectsForAPage;
    @XmlElement
    private long totalProjects;

    public ProjectPaginatedDto() {
    }

    public ProjectPaginatedDto(List<ProjectGetDto> projectsForAPage, long totalProjects) {
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