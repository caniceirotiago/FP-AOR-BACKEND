package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;


@Stateless
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(aor.fpbackend.bean.ReportBean.class);
    @EJB
    ProjectDao projectDao;
    @EJB
    LaboratoryDao laboratoryDao;

    // Consolidate Data Retrieval for Project Report

    public ReportSummaryDto getReportSummary() {
        ReportSummaryDto reportSummary = new ReportSummaryDto();

        reportSummary.setAverageMembersPerProject(getAverageMembersPerProject());
        reportSummary.setAverageProjectDuration(getAverageProjectDuration());
        reportSummary.setProjectCountByLocation(getProjectCountByLocation());
        reportSummary.setApprovedProjectsByLocation(getProjectsByLocationAndApproval(true));
        reportSummary.setCompletedProjectsByLocation(getProjectsByLocationAndState(ProjectStateEnum.FINISHED));
        reportSummary.setCanceledProjectsByLocation(getProjectsByLocationAndState(ProjectStateEnum.CANCELLED));

        return reportSummary;
    }



    // Projects count by laboratory location
    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        Map<LocationEnum, Long> projectCountByLaboratory = countProjectsByLocation();
        long totalProjects = projectCountByLaboratory.values().stream().mapToLong(Long::longValue).sum();

        List<ReportProjectsLocationDto> projectCountDtos = new ArrayList<>();

        for (Map.Entry<LocationEnum, Long> entry : projectCountByLaboratory.entrySet()) {
            double rawPercentage = (double) entry.getValue() * 100 / totalProjects;
            // Format the average to 2 decimal places
            String formattedPercentageStr = String.format(Locale.US, "%.2f", rawPercentage);
            double formattedPercentage = Double.parseDouble(formattedPercentageStr);
            projectCountDtos.add(new ReportProjectsLocationDto(entry.getKey(), entry.getValue(), formattedPercentage));
        }
        return projectCountDtos;
    }

    private Map<LocationEnum, Long> countProjectsByLocation() {
        List<Object[]> results = projectDao.countProjectsByLaboratory();

        Map<LocationEnum, Long> projectCountByLaboratory = new HashMap<>();
        for (Object[] result : results) {
            LocationEnum laboratoryLocation = (LocationEnum) result[0];
            Long count = (Long) result[1];
            projectCountByLaboratory.put(laboratoryLocation, count);
        }
        return projectCountByLaboratory;
    }

    // Average members per project
    public ReportAverageResultDto getAverageMembersPerProject() {
        Double averageMembers = projectDao.getAverageMembersPerProject();
        // Ensure the average is not null and handle it appropriately
        if (averageMembers == null) {
            averageMembers = 0.0;
        }
        // Format the average to 2 decimal places
        String formattedAverage = String.format(Locale.US,"%.2f", averageMembers);
        double roundedAverage = Double.parseDouble(formattedAverage);
        return new ReportAverageResultDto(roundedAverage);
    }

    // Average project duration
    public ReportAverageResultDto getAverageProjectDuration() {
        Double averageDuration = projectDao.getAverageProjectDuration();
        // Handle null case if no projects or durations found
        if (averageDuration == null) {
            averageDuration = 0.0; // or any default value as per your business logic
        }
        // Format the average to 2 decimal places
        String formattedAverage = String.format(Locale.US,"%.2f", averageDuration);
        double roundedAverage = Double.parseDouble(formattedAverage);
        return new ReportAverageResultDto(roundedAverage);
    }

    // Approved projects by location
    public List<ReportProjectsLocationDto> getProjectsByLocationAndApproval(boolean isApproved) {
        List<Object[]> results = projectDao.getProjectsByLocationAndApproval(isApproved);
        List<ReportProjectsLocationDto> projectsByLocationDtos = projectsByLocation(results);

        // Format percentage to 2 decimal places
        for (ReportProjectsLocationDto dto : projectsByLocationDtos) {
            String formattedPercentage = String.format(Locale.US, "%.2f", dto.getProjectPercentage());
            double roundedPercentage = Double.parseDouble(formattedPercentage);
            dto.setProjectPercentage(roundedPercentage);
        }
        return projectsByLocationDtos;
    }

    private List<ReportProjectsLocationDto> projectsByLocation(List<Object[]> results) {
        List<ReportProjectsLocationDto> projectsByLocationDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Double percentage = (Double) result[2];
            projectsByLocationDtos.add(new ReportProjectsLocationDto(location, count, percentage));
        }
        return projectsByLocationDtos;
    }


    // Approved projects by location
    public List<ReportProjectsLocationDto> getProjectsByLocationAndState(ProjectStateEnum state) {
        List<Object[]> results = projectDao.getProjectsByLocationAndState(state);

        List<ReportProjectsLocationDto> projectsByLocationDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Double percentage = (Double) result[2];
            projectsByLocationDtos.add(new ReportProjectsLocationDto(location, count, percentage));
        }
        return projectsByLocationDtos;
    }

}
