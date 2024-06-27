package aor.fpbackend.service;

import aor.fpbackend.bean.ReportBean;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.utils.PdfReportGenerator;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;

    @EJB
    PdfReportGenerator pdfReportGenerator;


    @GET
    @Path("/project/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
    public ReportSummaryDto getReportSummary() {
        return reportBean.getReportSummary();
    }

    @GET
    @Path("/project/summary/pdf")
    @Produces("application/pdf")
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
    public Response getReportSummaryPdf() {
        try {
            String saveFolderPath = reportBean.generatePdfReport();
            File file = new File(saveFolderPath);
            Response.ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=project_summary_report.pdf");
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("PDF generation failed").build();
        }
    }

}
