package aor.fpbackend.dto.Report;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;


@XmlRootElement
public class ReportProjectSummaryDto implements Serializable {

    @XmlElement
    private ReportAverageResultDto averageMembersPerProject;
    @XmlElement
    private ReportAverageResultDto averageProjectDuration;
    @XmlElement
    private List<ReportProjectsLocationDto> projectCountByLocation;
    @XmlElement
    private List<ReportProjectsLocationDto> approvedProjectsByLocation;
    @XmlElement
    private List<ReportProjectsLocationDto> completedProjectsByLocation;
    @XmlElement
    private List<ReportProjectsLocationDto> canceledProjectsByLocation;

    public ReportProjectSummaryDto() {
    }

    public ReportAverageResultDto getAverageMembersPerProject() {
        return averageMembersPerProject;
    }

    public void setAverageMembersPerProject(ReportAverageResultDto averageMembersPerProject) {
        this.averageMembersPerProject = averageMembersPerProject;
    }

    public ReportAverageResultDto getAverageProjectDuration() {
        return averageProjectDuration;
    }

    public void setAverageProjectDuration(ReportAverageResultDto averageProjectDuration) {
        this.averageProjectDuration = averageProjectDuration;
    }

    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        return projectCountByLocation;
    }

    public void setProjectCountByLocation(List<ReportProjectsLocationDto> projectCountByLocation) {
        this.projectCountByLocation = projectCountByLocation;
    }

    public List<ReportProjectsLocationDto> getApprovedProjectsByLocation() {
        return approvedProjectsByLocation;
    }

    public void setApprovedProjectsByLocation(List<ReportProjectsLocationDto> approvedProjectsByLocation) {
        this.approvedProjectsByLocation = approvedProjectsByLocation;
    }

    public List<ReportProjectsLocationDto> getCompletedProjectsByLocation() {
        return completedProjectsByLocation;
    }

    public void setCompletedProjectsByLocation(List<ReportProjectsLocationDto> completedProjectsByLocation) {
        this.completedProjectsByLocation = completedProjectsByLocation;
    }

    public List<ReportProjectsLocationDto> getCanceledProjectsByLocation() {
        return canceledProjectsByLocation;
    }

    public void setCanceledProjectsByLocation(List<ReportProjectsLocationDto> canceledProjectsByLocation) {
        this.canceledProjectsByLocation = canceledProjectsByLocation;
    }
}
