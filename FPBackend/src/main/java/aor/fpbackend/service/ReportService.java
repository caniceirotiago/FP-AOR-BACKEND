package aor.fpbackend.service;

import aor.fpbackend.bean.ReportBean;
import aor.fpbackend.dto.Report.ReportAverageMembersDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;


    @GET
    @Path("/count/location")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresMethodPermission(MethodEnum.ALL_KEYWORDS)
    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        return reportBean.getProjectCountByLocation();
    }

    @GET
    @Path("/average/members")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresMethodPermission(MethodEnum.ALL_KEYWORDS)
    public ReportAverageMembersDto getAverageMembersPerProject() {
        return reportBean.getAverageMembersPerProject();
    }

    @GET
    @Path("/approved/projects/location")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReportProjectsLocationDto> getApprovedProjectsByLocation() {
        return reportBean.getApprovedProjectsByLocation();
    }


}
