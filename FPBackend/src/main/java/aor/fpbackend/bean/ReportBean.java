package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.utils.PdfGenerator;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Stateless
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(aor.fpbackend.bean.ReportBean.class);
    @EJB
    ProjectDao projectDao;

    @EJB
    PdfGenerator pdfGenerator;

    // Generate PDF Report for Projects
    public String generateProjectPdfReport() throws IOException {
        ReportSummaryDto reportSummary = getReportSummary();
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
        pdfGenerator.generateProjectReport(reportSummary, pdfFilePath.toString());
        return pdfFilePath.toString();
    }

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

    // Average members per project
    public ReportAverageResultDto getAverageMembersPerProject() {
        Double averageMembers = projectDao.getAverageMembersPerProject();
        // Handle null case if no average found
        if (averageMembers == null) {
            averageMembers = 0.0; //
        }
        return new ReportAverageResultDto(averageMembers);
    }

    // Average project duration
    public ReportAverageResultDto getAverageProjectDuration() {
        Double averageDuration = projectDao.getAverageProjectDuration();
        // Handle null case if no average found
        if (averageDuration == null) {
            averageDuration = 0.0;
        }
        return new ReportAverageResultDto(averageDuration);
    }

    // Projects count by laboratory location
    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        List<Object[]> results = projectDao.countProjectsByLaboratory();
        List<ReportProjectsLocationDto> projectCountDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Double percentage = (Double) result[2];
            projectCountDtos.add(new ReportProjectsLocationDto(location, count, percentage));
        }
        return projectCountDtos;
    }

    // Approved projects by location and approval
    public List<ReportProjectsLocationDto> getProjectsByLocationAndApproval(boolean isApproved) {
        List<Object[]> results = projectDao.getProjectsByLocationAndApproval(isApproved);
        List<ReportProjectsLocationDto> projectsByLocationDtos = new ArrayList<>();
        for (Object[] result : results) {
            LocationEnum location = (LocationEnum) result[0];
            Long count = (Long) result[1];
            Double percentage = (Double) result[2];
            projectsByLocationDtos.add(new ReportProjectsLocationDto(location, count, percentage));
        }
        return projectsByLocationDtos;
    }

    // Approved projects by location and state
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


    // Generate PDF Report for Assets
    public String generateAssetPdfReport() throws IOException {
        ReportSummaryDto reportSummary = getReportSummary();

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
        pdfGenerator.generateAssetReport(reportSummary, pdfFilePath.toString());

        return pdfFilePath.toString();
    }


}
