package aor.fpbackend.service;

import aor.fpbackend.bean.TaskBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
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
    @RequiresProjectMemberPermission()
    public List<TaskGetDto> getAllTasksByProject(@PathParam("projectId") long projectId) {
        return taskBean.getTasksByProject(projectId);
    }

    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public TaskGetDto getTaskById(@PathParam("taskId") long taskId) {
        return taskBean.getTasksById(taskId);
    }

    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addTaskToProject(@Valid TaskCreateDto taskCreateDto) throws EntityNotFoundException, InputValidationException {
        if (taskCreateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        taskBean.addTask(taskCreateDto.getTitle(), taskCreateDto.getDescription(), taskCreateDto.getPlannedStartDate(),
                taskCreateDto.getPlannedEndDate(), taskCreateDto.getResponsibleId(), taskCreateDto.getProjectId());
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
    @RequiresProjectMemberPermission()
    public void addDependencyToTask(@Valid TaskAddDependencyDto addDependencyDto) throws EntityNotFoundException, InputValidationException {
        taskBean.addDependencyTask(addDependencyDto);
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void updateTask(@Valid TaskUpdateDto taskUpdateDto, @Context SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        taskBean.updateTask(taskUpdateDto, securityContext);
    }
}
