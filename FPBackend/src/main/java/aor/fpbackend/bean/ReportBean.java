package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectAssetDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.Report.ReportAssetSummaryDto;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportProjectSummaryDto;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.utils.PdfGenerator;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Stateless
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    ProjectDao projectDao;
    @EJB
    ProjectAssetDao projectAssetDao;
    @EJB
    PdfGenerator pdfGenerator;

    /**
     * Generates a PDF report for projects and saves it to a specified directory.
     *
     * <p>This method retrieves the project report summary, ensures the existence of the
     * directory where the PDF will be saved (using an environment variable or a default
     * relative directory if the environment variable is not set), constructs the file path
     * for the PDF, and generates the PDF report using the retrieved summary.
     *
     * @return The absolute path to the generated PDF report.
     * @throws IOException If an I/O error occurs during directory creation or PDF generation.
     */
    public String generateProjectPdfReport() throws IOException {
        ReportProjectSummaryDto projectReportSummary = getProjectReportSummary();
        // Use environment variable or default to a relative directory
        String baseDir = System.getenv("PDF_REPORT_DIR");
        if (baseDir == null || baseDir.isEmpty()) {
            baseDir = "generated-pdfs"; // Relative path to the project root
        }
        // Ensure the directory exists
        Path dirPath = Paths.get(baseDir).toAbsolutePath();
        Files.createDirectories(dirPath);
        // Construct the file path
        Path pdfFilePath = dirPath.resolve("project_summary_report.pdf");
        // Generate the PDF report
        pdfGenerator.generateProjectReport(projectReportSummary, pdfFilePath.toString());
        return pdfFilePath.toString();
    }

    /**
     * Consolidates data retrieval for the project report summary.
     *
     * <p>This method gathers various project-related metrics, including the average number of members per project,
     * average project duration, project counts by location, and counts of approved, completed, and canceled projects
     * by location. The collected data is assembled into a {@link ReportProjectSummaryDto} object, which is then returned.
     *
     * @return A {@link ReportProjectSummaryDto} object containing the consolidated project report summary.
     */
    public ReportProjectSummaryDto getProjectReportSummary() {
        ReportProjectSummaryDto reportSummary = new ReportProjectSummaryDto();
        reportSummary.setAverageMembersPerProject(getAverageMembersPerProject());
        reportSummary.setAverageProjectDuration(getAverageProjectDuration());
        reportSummary.setProjectCountByLocation(getProjectCountByLocation());
        reportSummary.setApprovedProjectsByLocation(getProjectsByLocationAndApproval(true));
        reportSummary.setCompletedProjectsByLocation(getProjectsByLocationAndState(ProjectStateEnum.FINISHED));
        reportSummary.setCanceledProjectsByLocation(getProjectsByLocationAndState(ProjectStateEnum.CANCELLED));
        return reportSummary;
    }

    /**
     * Retrieves the average number of members per project.
     *
     * <p>This method queries the database for the average number of members per project. If no average number is found,
     * it defaults to 0.0. The result is returned as a {@link ReportAverageResultDto} object.
     *
     * @return A {@link ReportAverageResultDto} object containing the average number of members per project.
     */
    public ReportAverageResultDto getAverageMembersPerProject() {
        Number averageMembers = projectDao.getAverageMembersPerProject();
        // Handle null case if no average found
        if (averageMembers == null) {
            averageMembers = 0.0;
        }
        return new ReportAverageResultDto(averageMembers.doubleValue());
    }

    /**
     * Retrieves the average duration of projects.
     *
     * <p>This method queries the database for the average duration of projects. If no average duration is found,
     * it defaults to 0.0. The result is returned as a {@link ReportAverageResultDto} object.
     *
     * @return A {@link ReportAverageResultDto} object containing the average project duration.
     */
    public ReportAverageResultDto getAverageProjectDuration() {
        Number averageDuration = projectDao.getAverageProjectDuration();
        // Handle null case if no average found
        if (averageDuration == null) {
            averageDuration = 0.0;
        }
        return new ReportAverageResultDto(averageDuration.doubleValue());
    }

    /**
     * Retrieves the count of projects by laboratory location.
     *
     * <p>This method queries the database for the count of projects grouped by laboratory location.
     * The results are then transformed into a list of {@link ReportProjectsLocationDto} objects, which contain
     * the location, count of projects, and percentage of total projects at that location.
     *
     * @return A list of {@link ReportProjectsLocationDto} objects representing the count and percentage of projects by location.
     */
    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        List<Object[]> results = projectDao.countProjectsByLaboratory();
        List<ReportProjectsLocationDto> projectCountDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Number percentage = (Number) result[2];
            projectCountDtos.add(new ReportProjectsLocationDto(location, count, percentage.doubleValue()));
        }
        return projectCountDtos;
    }

    /**
     * Retrieves a list of projects by their location and approval status.
     *
     * <p>This method queries the database for projects filtered by their approval status and groups them by location.
     * The results are then transformed into a list of {@link ReportProjectsLocationDto} objects, which contain
     * the location, count of projects, and percentage of total projects at that location.
     *
     * @param isApproved The approval status of the projects to filter by.
     * @return A list of {@link ReportProjectsLocationDto} objects representing the count and percentage of projects by location.
     */
    public List<ReportProjectsLocationDto> getProjectsByLocationAndApproval(boolean isApproved) {
        List<Object[]> results = projectDao.getProjectsByLocationAndApproval(isApproved);
        List<ReportProjectsLocationDto> projectsByLocationDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Number percentage = (Number) result[2];
            projectsByLocationDtos.add(new ReportProjectsLocationDto(location, count, percentage.doubleValue()));
        }
        return projectsByLocationDtos;
    }

    /**
     * Retrieves a list of approved projects by their location and state.
     *
     * <p>This method queries the database for projects filtered by a specified state and groups them by location.
     * The results are then transformed into a list of {@link ReportProjectsLocationDto} objects, which contain
     * the location, count of projects, and percentage of total projects at that location.
     *
     * @param state The state of the projects to filter by.
     * @return A list of {@link ReportProjectsLocationDto} objects representing the count and percentage of projects by location.
     */
    public List<ReportProjectsLocationDto> getProjectsByLocationAndState(ProjectStateEnum state) {
        List<Object[]> results = projectDao.getProjectsByLocationAndState(state);
        List<ReportProjectsLocationDto> projectsByLocationDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Number percentage = (Number) result[2];
            projectsByLocationDtos.add(new ReportProjectsLocationDto(location, count, percentage.doubleValue()));
        }
        return projectsByLocationDtos;
    }


    /**
     * Generates a PDF report for assets and saves it to a specified directory.
     *
     * <p>This method retrieves the asset report summary, ensures the existence of the
     * directory where the PDF will be saved (using an environment variable or a default
     * relative directory if the environment variable is not set), constructs the file path
     * for the PDF, and generates the PDF report using the retrieved summary.
     *
     * @return The absolute path to the generated PDF report.
     * @throws IOException If an I/O error occurs during directory creation or PDF generation.
     */
    public String generateAssetPdfReport() throws IOException {
        ReportAssetSummaryDto assetReportSummary = getAssetReportSummary();
        // Use environment variable or default to a relative directory
        String baseDir = System.getenv("PDF_REPORT_DIR");
        if (baseDir == null || baseDir.isEmpty()) {
            baseDir = "generated-pdfs"; // Relative path to the project root
        }
        // Ensure the directory exists
        Path dirPath = Paths.get(baseDir).toAbsolutePath();
        Files.createDirectories(dirPath);
        // Construct the file path
        Path pdfFilePath = dirPath.resolve("asset_summary_report.pdf");
        // Generate the PDF report
        pdfGenerator.generateAssetReport(assetReportSummary, pdfFilePath.toString());
        return pdfFilePath.toString();
    }

    /**
     * Consolidates data retrieval for the asset report summary.
     *
     * <p>This method gathers data related to asset usage, including the top projects by asset usage quantity
     * and the top assets by usage quantity. The collected data is assembled into a {@link ReportAssetSummaryDto}
     * object, which is then returned.
     *
     * @return A {@link ReportAssetSummaryDto} object containing the consolidated asset report summary.
     */
    public ReportAssetSummaryDto getAssetReportSummary() {
        // Retrieve the top projects by used quantity from the database
        List<Object[]> topProjectsByUsedQuantity = projectAssetDao.getTopProjectsByUsedQuantity();
        // Retrieve the top assets by used quantity from the database
        List<Object[]> topAssetsByUsedQuantity = projectAssetDao.getTopAssetsByUsedQuantity();
        // Create a new ReportAssetSummaryDto object to hold the summary data
        ReportAssetSummaryDto assetReportSummary = new ReportAssetSummaryDto();
        // Set the list of top projects by used quantity
        assetReportSummary.setTopProjectsByUsedQuantity(topProjectsByUsedQuantity);
        // Set the list of top assets by used quantity
        assetReportSummary.setTopAssetsByUsedQuantity(topAssetsByUsedQuantity);
        return assetReportSummary;
    }

}
