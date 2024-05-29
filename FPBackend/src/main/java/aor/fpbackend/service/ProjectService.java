package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.ProjectCreateDto;
import aor.fpbackend.dto.ProjectGetDto;
import aor.fpbackend.dto.ProjectInviteDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;


@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_PROJECT)
    public void createProject(@Valid ProjectCreateDto projectCreateDto) throws EntityNotFoundException, AttributeAlreadyExistsException, InputValidationException {
        projectBean.createProject(projectCreateDto);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_PROJECTS)
    public ArrayList<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }

    @GET
    @Path("/info/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.PROJECT_BY_ID)
    public ProjectGetDto getProjectDetails(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return projectBean.getProjectDetailsById(projectId);
    }

    @PUT
    @Path("/send/invite")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.INVITE_TO_PROJECT)
    public void sendInvite(@Valid ProjectInviteDto projectInviteDto) throws UserNotFoundException, InputValidationException {
        projectBean.sendInviteToUser(projectInviteDto);
    }


}
