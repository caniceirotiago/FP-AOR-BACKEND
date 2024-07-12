package aor.fpbackend.service;

import aor.fpbackend.bean.TaskBean;
import aor.fpbackend.dto.Task.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.TaskStateEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.net.UnknownHostException;
import java.util.List;

/**
 * TaskService is a JAX-RS resource class that provides RESTful endpoints for task management,
 * including task creation, updates, dependencies, and retrievals by project.
 */
@Path("/tasks")
public class TaskService {

    @EJB
    TaskBean taskBean;

    /**
     * Retrieves all tasks for a given project.
     *
     * @param projectId the project ID.
     * @return a list of task data transfer objects.
     * @throws EntityNotFoundException if the project is not found.
     */
    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<TaskGetDto> getAllTasksByProject(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return taskBean.getTasksByProject(projectId);
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param taskId the task ID.
     * @return the task data transfer object.
     * @throws EntityNotFoundException if the task is not found.
     */
    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASKS_BY_ID)
    public TaskGetDto getTaskById(@PathParam("taskId") long taskId) throws EntityNotFoundException {
        return taskBean.getTasksById(taskId);
    }

    /**
     * Adds a task to a project.
     *
     * @param taskCreateDto the task creation data transfer object.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws InputValidationException if the input data is invalid.
     * @throws UnknownHostException if an unknown host error occurs.
     * @throws ElementAssociationException if there is an association error.
     */
    @POST
    @Path("/add/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addTaskToProject(@Valid TaskCreateDto taskCreateDto) throws EntityNotFoundException, InputValidationException, UnknownHostException, ElementAssociationException {
        taskBean.addTask(taskCreateDto.getTitle(), taskCreateDto.getDescription(), taskCreateDto.getPlannedStartDate(),
                taskCreateDto.getPlannedEndDate(), taskCreateDto.getResponsibleId(), taskCreateDto.getProjectId());
    }

    /**
     * Deletes a task.
     *
     * @param taskId the task ID.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the task is not found.
     */
    @DELETE
    @Path("/{taskId}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void deleteTask(@PathParam("taskId") long taskId, @Context SecurityContext securityContext) throws EntityNotFoundException {
        taskBean.deleteTask(taskId, securityContext);
    }

    /**
     * Adds a dependency to a task.
     *
     * @param projectId the project ID.
     * @param dependencyDto the task dependency data transfer object.
     * @throws EntityNotFoundException if the project or task is not found.
     * @throws InputValidationException if the input data is invalid.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @PUT
    @Path("/add/dependency/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addDependencyToTask(@PathParam("projectId") long projectId, @Valid TaskDependencyDto dependencyDto) throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        taskBean.addDependencyTask(projectId, dependencyDto);
    }

    /**
     * Removes a dependency from a task.
     *
     * @param projectId the project ID.
     * @param dependencyDto the task dependency data transfer object.
     * @throws EntityNotFoundException if the project or task is not found.
     * @throws DatabaseOperationException if a database error occurs.
     * @throws InputValidationException if the input data is invalid.
     */
    @DELETE
    @Path("/dependency/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeDependencyFromTask(@PathParam("projectId") long projectId, @Valid TaskDependencyDto dependencyDto) throws EntityNotFoundException, DatabaseOperationException, InputValidationException {
        taskBean.removeDependencyTask(projectId, dependencyDto);
    }

    /**
     * Updates a task.
     *
     * @param taskUpdateDto the task update data transfer object.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the task is not found.
     * @throws InputValidationException if the input data is invalid.
     * @throws UserNotFoundException if the user is not found.
     */
    @PUT
    @Path("{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateTask(@Valid TaskUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UserNotFoundException {
        taskBean.updateTask(taskUpdateDto, securityContext);
    }

    /**
     * Updates a task with detailed information.
     *
     * @param taskUpdateDto the task detailed update data transfer object.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the task is not found.
     * @throws InputValidationException if the input data is invalid.
     * @throws UserNotFoundException if the user is not found.
     * @throws UnknownHostException if an unknown host error occurs.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @PUT
    @Path("/detailed/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateTaskDetailed(@Valid TaskDetailedUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UserNotFoundException, UnknownHostException, DatabaseOperationException {
        taskBean.taskDetailedUpdate(taskUpdateDto, securityContext);
    }

    /**
     * Retrieves a list of possible task states.
     *
     * @return a list of task state enums.
     */
    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TaskStateEnum> getTaskStates() {
        return taskBean.getEnumListTaskStates();
    }

}
