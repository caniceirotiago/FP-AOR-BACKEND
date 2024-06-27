package aor.fpbackend.service;

import aor.fpbackend.bean.ReportBean;
import aor.fpbackend.dto.Report.ReportAverageResultDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/reports")
public class ReportService {

    @EJB
    ReportBean reportBean;


    @GET
    @Path("/average/members")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresMethodPermission(MethodEnum.REPORTS)
    public ReportAverageResultDto getAverageMembersPerProject() {
        return reportBean.getAverageMembersPerProject();
    }

    @GET
    @Path("/average/duration")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresMethodPermission(MethodEnum.REPORTS)
    public ReportAverageResultDto getAverageProjectDuration() {
        return reportBean.getAverageProjectDuration();
    }

    @GET
    @Path("/count/location")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresMethodPermission(MethodEnum.REPORTS)
    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        return reportBean.getProjectCountByLocation();
    }

    @GET
    @Path("/approved/projects/location")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReportProjectsLocationDto> getProjectsByLocation() {
        return reportBean.getProjectsByLocationAndApproval(true);
    }

    @GET
    @Path("/completed/projects/location")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReportProjectsLocationDto> getCompletedProjectsByLocation() {
        return reportBean.getProjectsByLocationAndState(ProjectStateEnum.FINISHED);
    }

    @GET
    @Path("/canceled/projects/location")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReportProjectsLocationDto> getCanceledProjectsByLocation() {
        return reportBean.getProjectsByLocationAndState(ProjectStateEnum.CANCELLED);
    }

}
