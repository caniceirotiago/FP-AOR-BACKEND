package aor.fpbackend.service;

import aor.fpbackend.bean.ReportBean;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;


    @GET
    @Path("/project/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_REPORTS)
    public ReportSummaryDto getReportSummary() {
        return reportBean.getReportSummary();
    }
    

}
