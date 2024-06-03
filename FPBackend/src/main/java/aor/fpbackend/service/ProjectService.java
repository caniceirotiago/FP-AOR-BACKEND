package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.ProjectStateEnum;
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
    @GET
    @Path("/info/project-states")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_PROJECTS)
    public List<ProjectStateEnum> getProjectStates() {
        List<ProjectStateEnum> projectStateEnums = new ArrayList<>();
        for (ProjectStateEnum projectStateEnum : ProjectStateEnum.values()) {
            projectStateEnums.add(projectStateEnum);
        }
        return projectStateEnums;
    }
    @GET
    @Path("/info/project-roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_PROJECTS)
    public List<ProjectRoleEnum> getProjectRoles() {
        List<ProjectRoleEnum> projectRoleEnums = new ArrayList<>();
        for (ProjectRoleEnum projectRoleEnum : ProjectRoleEnum.values()) {
            projectRoleEnums.add(projectRoleEnum);
        }
        return projectRoleEnums;
    }
    @PUT
    @Path("/project-role")
    @Consumes(MediaType.APPLICATION_JSON)
    //TODO: RequiresPermission especifica de projeto e role desse utilizador no projeto
    public void updateProjectRole(@Valid ProjectRoleUpdateDto projectRoleUpdateDto) throws EntityNotFoundException, InputValidationException {
        projectBean.updateProjectMembershipRole(projectRoleUpdateDto);
    }
    @PUT
    @Path("/ask/join")
    @Consumes(MediaType.APPLICATION_JSON)
    //TODO: RequiresPermission especifica de projeto e role desse utilizador no projeto
    public void askToJoinProject(@Valid ProjectAskJoinDto projectAskJoinDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UserNotFoundException {
        projectBean.askToJoinProject(projectAskJoinDto, securityContext);
    }
    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateProject(@Valid ProjectUpdateDto projectUpdateDto) throws EntityNotFoundException, InputValidationException {
        projectBean.updateProject(projectUpdateDto);
    }

//    @PUT
//    @Path("/send/invite")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.INVITE_TO_PROJECT)
//    public void sendInvite(@Valid ProjectInviteDto projectInviteDto) throws UserNotFoundException, InputValidationException {
//        projectBean.sendInviteToUser(projectInviteDto);
//    }


}
