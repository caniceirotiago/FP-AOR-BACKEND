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

@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;


    @GET
    @Path("/project/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
    public ReportProjectSummaryDto getProjectReportSummary() {
        return reportBean.getProjectReportSummary();
    }

    @GET
    @Path("/asset/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
    public ReportAssetSummaryDto getAssetReportSummary() {
        return reportBean.getAssetReportSummary();
    }

    @GET
    @Path("/project/summary/pdf")
    @Produces("application/pdf")
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
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

    @GET
    @Path("/asset/summary/pdf")
    @Produces("application/pdf")
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
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
