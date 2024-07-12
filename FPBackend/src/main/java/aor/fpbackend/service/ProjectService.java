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

/**
 * ProjectService is a JAX-RS resource class that provides RESTful endpoints for managing projects,
 * including creation, retrieval, updating, and approval of projects.
 */
@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;
    /**
     * Creates a new project.
     *
     * @param projectCreateDto the project creation DTO.
     * @param securityContext  the security context.
     * @throws EntityNotFoundException       if the entity is not found.
     * @throws DuplicatedAttributeException  if the attribute is duplicated.
     * @throws InputValidationException      if there is an input validation error.
     * @throws UserNotFoundException         if the user is not found.
     * @throws ElementAssociationException   if there is an element association error.
     * @throws UnknownHostException          if there is an unknown host exception.
     * @throws DatabaseOperationException    if there is a database operation error.
     */
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_PROJECT)
    public void createProject(@Valid ProjectCreateDto projectCreateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, InputValidationException, UserNotFoundException, ElementAssociationException, UnknownHostException, DatabaseOperationException {
        projectBean.createProject(projectCreateDto, securityContext);
    }

    /**
     * Retrieves all projects.
     *
     * @return a list of ProjectGetDto.
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_PROJECTS)
    public List<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }

    /**
     * Retrieves all project IDs.
     *
     * @return a list of project IDs.
     */
    @GET
    @Path("/all/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getAllProjectsIds() {
        return projectBean.getAllProjectsIds();
    }

    /**
     * Retrieves filtered projects based on the query parameters.
     *
     * @param page    the page number.
     * @param pageSize the size of the page.
     * @param uriInfo  the URI info.
     * @return a paginated DTO of filtered projects.
     * @throws InputValidationException if there is an input validation error.
     */
    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectPaginatedDto getFilteredProjects(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("8") int pageSize,
            @Context UriInfo uriInfo) throws InputValidationException {
        return projectBean.getFilteredProjects(page, pageSize, uriInfo);
    }

    /**
     * Retrieves project details by project ID.
     *
     * @param projectId the project ID.
     * @return a ProjectGetDto.
     * @throws EntityNotFoundException if the entity is not found.
     */
    @GET
    @Path("/info/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_BY_ID)
    public ProjectGetDto getProjectDetails(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return projectBean.getProjectDetailsById(projectId);
    }

    /**
     * Retrieves all project states.
     *
     * @return a list of ProjectStateEnum.
     */

    @GET
    @Path("/enum/states")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_ENUMS)
    public List<ProjectStateEnum> getProjectStates() {
        return projectBean.getEnumListProjectStates();
    }

    /**
     * Retrieves all project roles.
     *
     * @return a list of ProjectRoleEnum.
     */
    @GET
    @Path("/enum/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_ENUMS)
    public List<ProjectRoleEnum> getProjectRoles() {
        return projectBean.getEnumListProjectRoles();
    }

    /**
     * Retrieves project logs by project ID.
     *
     * @param projectId the project ID.
     * @return a list of ProjectLogGetDto.
     */
    @GET
    @Path("/logs/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<ProjectLogGetDto> getProjectLogs(@PathParam("projectId") long projectId) {
        return projectBean.getListProjectLogs(projectId);
    }

    /**
     * Creates a new project log.
     *
     * @param projectId          the project ID.
     * @param createProjectLogDto the project log creation DTO.
     * @param securityContext    the security context.
     * @throws EntityNotFoundException if the entity is not found.
     */

    @POST
    @Path("/logs/create/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void createProjectLog(@PathParam("projectId") long projectId, @Valid ProjectLogCreateDto createProjectLogDto, @Context SecurityContext securityContext) throws EntityNotFoundException {
        projectBean.createManualProjectLog(projectId, createProjectLogDto, securityContext);
    }

    /**
     * Approves a project.
     *
     * @param projectApproveDto the project approval DTO.
     * @param securityContext   the security context.
     * @throws EntityNotFoundException if the entity is not found.
     * @throws UnknownHostException    if there is an unknown host exception.
     */
    @PUT
    @Path("/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.PROJECT_APPROVE)
    public void approveProject(@Valid ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException {
        projectBean.approveProject(projectApproveDto, securityContext);
    }

    /**
     * Updates the role of a project member.
     *
     * @param projectId          the project ID.
     * @param projectRoleUpdateDto the project role update DTO.
     * @param securityContext    the security context.
     * @throws EntityNotFoundException        if the entity is not found.
     * @throws InputValidationException       if there is an input validation error.
     * @throws DatabaseOperationException     if there is a database operation error.
     * @throws ElementAssociationException    if there is an element association error.
     */
    @PUT
    @Path("/role/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void updateProjectRole(@PathParam("projectId") long projectId, @Valid ProjectRoleUpdateDto projectRoleUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, DatabaseOperationException, ElementAssociationException {
        projectBean.updateProjectMembershipRole(projectId, projectRoleUpdateDto, securityContext);
    }

    /**
     * Updates a project.
     *
     * @param projectId         the project ID.
     * @param projectUpdateDto  the project update DTO.
     * @param securityContext   the security context.
     * @throws EntityNotFoundException        if the entity is not found.
     * @throws InputValidationException       if there is an input validation error.
     * @throws UnknownHostException           if there is an unknown host exception.
     */
    @PUT
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateProject(@PathParam("projectId") long projectId, @Valid ProjectUpdateDto projectUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UnknownHostException {
        projectBean.updateProject(projectId, projectUpdateDto, securityContext);
    }
}
