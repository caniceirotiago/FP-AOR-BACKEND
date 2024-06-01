package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.ProjectCreateDto;
import aor.fpbackend.dto.ProjectGetDto;
import aor.fpbackend.dto.ProjectInviteDto;
import aor.fpbackend.dto.ProjectsPaginatedDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;


@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_PROJECT)
    public void createProject(@Valid ProjectCreateDto projectCreateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, AttributeAlreadyExistsException, InputValidationException, UserNotFoundException {
        projectBean.createProject(projectCreateDto, securityContext);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_PROJECTS)
    public List<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }
    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_PROJECTS)
    public ProjectsPaginatedDto getFilteredProjects(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Context UriInfo uriInfo) {
        return projectBean.getFilteredProjects(page, pageSize, uriInfo);
    }

    @GET
    @Path("/info/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.PROJECT_BY_ID)
    public ProjectGetDto getProjectDetails(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return projectBean.getProjectDetailsById(projectId);
    }

//    @PUT
//    @Path("/send/invite")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.INVITE_TO_PROJECT)
//    public void sendInvite(@Valid ProjectInviteDto projectInviteDto) throws UserNotFoundException, InputValidationException {
//        projectBean.sendInviteToUser(projectInviteDto);
//    }


}
