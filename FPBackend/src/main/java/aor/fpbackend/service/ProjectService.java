package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.Project.*;
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

import java.net.UnknownHostException;
import java.util.List;


@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_PROJECT)
    public void createProject(@Valid ProjectCreateDto projectCreateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, InputValidationException, UserNotFoundException, ElementAssociationException, UnknownHostException, DatabaseOperationException {
        projectBean.createProject(projectCreateDto, securityContext);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_PROJECTS)
    public List<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }

    @GET
    @Path("/all/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getAllProjectsIds() {
        return projectBean.getAllProjectsIds();
    }

    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectPaginatedDto getFilteredProjects(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("8") int pageSize,
            @Context UriInfo uriInfo) throws InputValidationException {
        return projectBean.getFilteredProjects(page, pageSize, uriInfo);
    }

    @GET
    @Path("/info/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_BY_ID)
    public ProjectGetDto getProjectDetails(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return projectBean.getProjectDetailsById(projectId);
    }
    
    @GET
    @Path("/enum/states")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_ENUMS)
    public List<ProjectStateEnum> getProjectStates() {
        return projectBean.getEnumListProjectStates();
    }

    @GET
    @Path("/enum/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_ENUMS)
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
    @RequiresMethodPermission(MethodEnum.PROJECT_APPROVE)
    public void approveProject(@Valid ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException {
        projectBean.approveProject(projectApproveDto, securityContext);
    }

    @PUT
    @Path("/role/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void updateProjectRole(@PathParam("projectId") long projectId, @Valid ProjectRoleUpdateDto projectRoleUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, DatabaseOperationException, ElementAssociationException {
        projectBean.updateProjectMembershipRole(projectId, projectRoleUpdateDto, securityContext);
    }

    @PUT
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateProject(@PathParam("projectId") long projectId, @Valid ProjectUpdateDto projectUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UnknownHostException {
        projectBean.updateProject(projectId, projectUpdateDto, securityContext);
    }
}
