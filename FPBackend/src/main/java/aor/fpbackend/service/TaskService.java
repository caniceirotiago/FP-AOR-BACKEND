package aor.fpbackend.service;

import aor.fpbackend.bean.TaskBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;


@Path("/tasks")
public class TaskService {

    @EJB
    TaskBean taskBean;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_TASKS)
    public List<TaskGetDto> getAllTasks() {
        return taskBean.getTasks();
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASKS_BY_PROJECT)
    public List<TaskGetDto> getAllTasksByProject(@PathParam("projectId") long projectId) {
        return taskBean.getTasksByProject(projectId);
    }

    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    //@RequiresPermission(MethodEnum.TASK_BY_ID)
    public TaskGetDto getTaskById(@PathParam("taskId") long taskId) {
        return taskBean.getTasksById(taskId);
    }

    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_TASK)
    public void addTaskToProject(@Valid TaskCreateDto taskCreateDto) throws EntityNotFoundException, InputValidationException {
        taskBean.addTask(taskCreateDto);
    }

    @PUT
    @Path("/add/executor")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASK_USER)
    public void addUserToTask(@Valid TaskAddUserDto taskAddUserDto) throws EntityNotFoundException, InputValidationException {
        taskBean.addUserTask(taskAddUserDto);
    }

    @PUT
    @Path("/add/dependency")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASK_DEPENDENCY)
    public void addDependencyToTask(@Valid TaskAddDependencyDto addDependencyDto) throws EntityNotFoundException, InputValidationException {
        taskBean.addDependencyTask(addDependencyDto);
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.TASK_UPDATE)
    public void updateTask(@Valid TaskUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        taskBean.updateTask(taskUpdateDto, securityContext);
    }
}
