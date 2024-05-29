package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.TaskDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.TaskEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.TaskStateEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Stateless
public class TaskBean implements Serializable {
    @EJB
    TaskDao taskDao;
    @EJB
    ProjectDao projectDao;
    @EJB
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(TaskBean.class);

    @Transactional
    public void addTask(TaskAddDto taskAddDto) throws EntityNotFoundException, InputValidationException {
        if (taskAddDto==null){
            throw new InputValidationException("Invalid Dto");
        }
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(taskAddDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        UserEntity taskResponsible = userDao.findUserById(taskAddDto.getResponsibleId());
        if (taskResponsible == null) {
            throw new EntityNotFoundException("User not found");
        }
        // Avoid duplicate titles
        if (taskDao.checkTitleExist(taskAddDto.getTitle())) {
            throw new InputValidationException("Duplicated title");
        }
        // Fetch additional executers by their IDs
        Set<UserEntity> additionalExecuters = new HashSet<>();
        for (UsernameDto user : taskAddDto.getAddExecuters()) {
            UserEntity additionalExecuter = userDao.findUserById(user.getId());
            if (additionalExecuter == null) {
                throw new EntityNotFoundException("Additional executer not found");
            }
            additionalExecuters.add(additionalExecuter);
        }
        // Fetch dependent tasks by their IDs
        Set<TaskEntity> dependentTasks = new HashSet<>();
        for (Long taskId : taskAddDto.getDependentTasks()) {
            TaskEntity dependentTask = taskDao.findTaskById(taskId);
            if (dependentTask == null) {
                throw new EntityNotFoundException("Dependent task not found");
            }
            dependentTasks.add(dependentTask);
        }
        // Create a new task entity
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTitle(taskAddDto.getTitle());
        taskEntity.setDescription(taskAddDto.getDescription());
        taskEntity.setPlannedStartDate(taskAddDto.getPlannedStartDate());
        taskEntity.setCreationDate(Instant.now());
        taskEntity.setPlannedEndDate(taskAddDto.getPlannedEndDate());
        taskEntity.setState(TaskStateEnum.PLANNED); // Default state
        taskEntity.setResponsibleUser(taskResponsible);
        taskEntity.setAdditionalExecuters(additionalExecuters);
        taskEntity.setDependentTasks(dependentTasks);
        // Associate the task with the project
        taskEntity.setProject(projectEntity);
        taskDao.persist(taskEntity);
    }

    public List<TaskGetDto> getTasks() {
        return convertTaskEntityListToTaskDtoList(taskDao.findAllTasks());
    }

    public List<TaskGetDto> getTasksByProject(long projectId) {
        return convertTaskEntityListToTaskDtoList(taskDao.getTasksByProjectId(projectId));
    }

    public TaskGetDto convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        TaskGetDto taskGetDto = new TaskGetDto();
        taskGetDto.setId(taskEntity.getId());
        taskGetDto.setTitle(taskEntity.getTitle());
        taskGetDto.setDescription(taskEntity.getDescription());
        taskGetDto.setPlannedStartDate(taskEntity.getPlannedStartDate());
        taskGetDto.setPlannedEndDate(taskEntity.getPlannedEndDate());
        taskGetDto.setState(taskEntity.getState());
        taskGetDto.setResponsibleId(taskEntity.getResponsibleUser().getId());
        taskGetDto.setProjectId(taskEntity.getProject().getId());
        return taskGetDto;
    }

    public List<TaskGetDto> convertTaskEntityListToTaskDtoList(List<TaskEntity> taskEntities) {
        List<TaskGetDto> taskGetDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            TaskGetDto taskGetDto = convertTaskEntityToTaskDto(taskEntity);
            taskGetDtos.add(taskGetDto);
        }
        return taskGetDtos;
    }
}