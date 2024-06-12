package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;


@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public void createProject(@Valid ProjectCreateDto projectCreateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, InputValidationException, UserNotFoundException {
        projectBean.createProject(projectCreateDto, securityContext);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public List<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }
    @GET
    @Path("/all/ids")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public List<Long> getAllProjectsIds() {
        return projectBean.getAllProjectsIds();
    }

    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public ProjectPaginatedDto getFilteredProjects(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Context UriInfo uriInfo) {
        return projectBean.getFilteredProjects(page, pageSize, uriInfo);
    }

    @GET
    @Path("/info/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public ProjectGetDto getProjectDetails(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return projectBean.getProjectDetailsById(projectId);
    }
    @GET
    @Path("/enum/states")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public List<ProjectStateEnum> getProjectStates() {
        return projectBean.getEnumListProjectStates();
    }

    @GET
    @Path("/enum/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_PROJECTS)
    public List<ProjectRoleEnum> getProjectRoles() {
        return projectBean.getEnumListProjectRoles();
    }

    @GET
    @Path("/logs/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<ProjectLogGetDto> getProjectLogs(@PathParam("projectId") long projectId) {
        return projectBean.getListProjectLogs(projectId);
    }

    @PUT
    @Path("/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADMIN_LEVEL_PROJECTS)
    public void approveProject(@Valid ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UnauthorizedAccessException {
        projectBean.approveProject(projectApproveDto, securityContext);
    }

    @PUT
    @Path("/role/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void updateProjectRole(@PathParam("projectId") long projectId, @Valid ProjectRoleUpdateDto projectRoleUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        projectBean.updateProjectMembershipRole(projectId, projectRoleUpdateDto, securityContext);
    }

    @PUT
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateProject(@PathParam("projectId") long projectId, @Valid ProjectUpdateDto projectUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        projectBean.updateProject(projectId, projectUpdateDto, securityContext);
    }
}
