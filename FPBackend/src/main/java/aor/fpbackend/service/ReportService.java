package aor.fpbackend.service;

import aor.fpbackend.bean.ReportBean;
import aor.fpbackend.dto.Report.ReportAssetSummaryDto;
import aor.fpbackend.dto.Report.ReportProjectSummaryDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
/**
 * ReportService is a JAX-RS resource class that provides RESTful endpoints for generating and retrieving
 * project and asset reports in both JSON and PDF formats.
 */
@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;

    /**
     * Retrieves a summary of project reports in JSON format.
     *
     * @return a ReportProjectSummaryDto containing project report summary data.
     */
    @GET
    @Path("/project/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECTS_REPORT)
    public ReportProjectSummaryDto getProjectReportSummary() {
        return reportBean.getProjectReportSummary();
    }

    /**
     * Retrieves a summary of asset reports in JSON format.
     *
     * @return a ReportAssetSummaryDto containing asset report summary data.
     */
    @GET
    @Path("/asset/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_REPORT)
    public ReportAssetSummaryDto getAssetReportSummary() {
        return reportBean.getAssetReportSummary();
    }

    /**
     * Generates and retrieves a PDF summary of project reports.
     *
     * @return a Response containing the project report summary PDF file.
     */
    @GET
    @Path("/project/summary/pdf")
    @Produces("application/pdf")
    @RequiresMethodPermission(MethodEnum.PROJECTS_REPORT)
    public Response getProjectReportSummaryPdf() {
        try {
            String saveFolderPath = reportBean.generateProjectPdfReport();
            File file = new File(saveFolderPath);
            Response.ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=project_summary_report.pdf");
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("PDF generation failed").build();
        }
    }

    /**
     * Generates and retrieves a PDF summary of asset reports.
     *
     * @return a Response containing the asset report summary PDF file.
     */
    @GET
    @Path("/asset/summary/pdf")
    @Produces("application/pdf")
    @RequiresMethodPermission(MethodEnum.ASSETS_REPORT)
    public Response getAssetReportSummaryPdf() {
        try {
            String saveFolderPath = reportBean.generateAssetPdfReport();
            File file = new File(saveFolderPath);
            Response.ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=asset_summary_report.pdf");
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("PDF generation failed").build();
        }
    }
}
