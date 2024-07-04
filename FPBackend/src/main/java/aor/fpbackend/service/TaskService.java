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


@Path("/tasks")
public class TaskService {

    @EJB
    TaskBean taskBean;

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<TaskGetDto> getAllTasksByProject(@PathParam("projectId") long projectId) throws EntityNotFoundException {
        return taskBean.getTasksByProject(projectId);
    }

    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASKS_BY_ID)
    public TaskGetDto getTaskById(@PathParam("taskId") long taskId) throws EntityNotFoundException {
        return taskBean.getTasksById(taskId);
    }

    @POST
    @Path("/add/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addTaskToProject(@Valid TaskCreateDto taskCreateDto) throws EntityNotFoundException, InputValidationException, UnknownHostException, ElementAssociationException {
        taskBean.addTask(taskCreateDto.getTitle(), taskCreateDto.getDescription(), taskCreateDto.getPlannedStartDate(),
                taskCreateDto.getPlannedEndDate(), taskCreateDto.getResponsibleId(), taskCreateDto.getProjectId());
    }

    @DELETE
    @Path("/{taskId}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void deleteTask(@PathParam("taskId") long taskId, @Context SecurityContext securityContext) throws EntityNotFoundException {
        taskBean.deleteTask(taskId, securityContext);
    }

    @PUT
    @Path("/add/dependency/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addDependencyToTask(@PathParam("projectId") long projectId, @Valid TaskDependencyDto dependencyDto) throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        taskBean.addDependencyTask(projectId, dependencyDto);
    }

    @DELETE
    @Path("/dependency/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeDependencyFromTask(@PathParam("projectId") long projectId, @Valid TaskDependencyDto dependencyDto) throws EntityNotFoundException, DatabaseOperationException, InputValidationException {
        taskBean.removeDependencyTask(projectId, dependencyDto);
    }

    @PUT
    @Path("{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateTask(@Valid TaskUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UserNotFoundException {
        taskBean.updateTask(taskUpdateDto, securityContext);
    }

    @PUT
    @Path("/detailed/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateTaskDetailed(@Valid TaskDetailedUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UserNotFoundException, UnknownHostException, DatabaseOperationException {
        taskBean.taskDetailedUpdate(taskUpdateDto, securityContext);
    }

    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TaskStateEnum> getTaskStates() {
        return taskBean.getEnumListTaskStates();
    }

}
