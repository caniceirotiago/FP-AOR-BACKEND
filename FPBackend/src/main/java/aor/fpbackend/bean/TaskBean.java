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

import java.time.temporal.ChronoUnit;

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


    public List<TaskGetDto> getTasks() {
        return convertTaskEntityListToTaskDtoList(taskDao.findAllTasks());
    }

    public List<TaskGetDto> getTasksByProject(long projectId) {
        return convertTaskEntityListToTaskDtoList(taskDao.getTasksByProjectId(projectId));
    }

    @Transactional
    public void addTask(TaskCreateDto taskCreateDto) throws EntityNotFoundException, InputValidationException {
        if (taskCreateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(taskCreateDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        UserEntity taskResponsible = userDao.findUserById(taskCreateDto.getResponsibleId());
        if (taskResponsible == null) {
            throw new EntityNotFoundException("User not found");
        }
        // Avoid duplicate titles
        if (taskDao.checkTitleExist(taskCreateDto.getTitle())) {
            throw new InputValidationException("Duplicated title");
        }
        // Create a new task entity
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTitle(taskCreateDto.getTitle());
        taskEntity.setDescription(taskCreateDto.getDescription());
        taskEntity.setPlannedStartDate(taskCreateDto.getPlannedStartDate());
        taskEntity.setCreationDate(Instant.now());
        taskEntity.setPlannedEndDate(taskCreateDto.getPlannedEndDate());
        taskEntity.setState(TaskStateEnum.PLANNED); // Default state
        taskEntity.setResponsibleUser(taskResponsible);
        // Associate the task with the project
        taskEntity.setProject(projectEntity);
        taskDao.persist(taskEntity);
    }

    @Transactional
    public void addUserTask(TaskAddUsersDto taskAddUsersDto) throws InputValidationException, EntityNotFoundException {
        if (taskAddUsersDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskAddUsersDto.getTaskId());
        if (taskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        // Fetch registered executors by their IDs
        Set<UserEntity> registeredExecutors = taskEntity.getRegisteredExecutors();
        for (UsernameDto user : taskAddUsersDto.getRegisteredExecutors()) {
            UserEntity registeredExecutor = userDao.findUserById(user.getId());
            if (registeredExecutor == null) {
                throw new EntityNotFoundException("Registered executor not found");
            }
            registeredExecutors.add(registeredExecutor);
            registeredExecutor.getTasksAsExecutor().add(taskEntity); // Update the other side of the relation
        }
        // Add additional executors (non-registered)
        if (taskAddUsersDto.getNonRegisteredExecutors() != null) {
            taskEntity.setAdditionalExecutors(taskAddUsersDto.getNonRegisteredExecutors());
        }
    }

    @Transactional
    public void addDependencyTask(TaskAddDependencyDto addDependencyDto) throws InputValidationException, EntityNotFoundException {
        if (addDependencyDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        TaskEntity mainTaskEntity = taskDao.findTaskById(addDependencyDto.getMainTaskId());
        TaskEntity dependentTaskEntity = taskDao.findTaskById(addDependencyDto.getDependentTaskId());
        if (mainTaskEntity == null || dependentTaskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        defineDependency(mainTaskEntity, dependentTaskEntity);
    }

    private void defineDependency(TaskEntity mainTaskEntity, TaskEntity dependentTaskEntity) {
        // Update dependent tasks of the main task
        Set<TaskEntity> dependentTasks = mainTaskEntity.getDependentTasks();
        dependentTasks.add(dependentTaskEntity);
        // Update prerequisites of the dependent task
        Set<TaskEntity> prerequisites = dependentTaskEntity.getPrerequisites();
        prerequisites.add(mainTaskEntity);
    }

    public void updateTask(TaskUpdateDto taskUpdateDto) throws InputValidationException, EntityNotFoundException {
        if (taskUpdateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskUpdateDto.getTaskId());
        if (taskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        // Validate planned dates if both are present
        if (taskUpdateDto.getPlannedStartDate() != null && taskUpdateDto.getPlannedEndDate() != null) {
            if (taskUpdateDto.getPlannedEndDate().isBefore(taskUpdateDto.getPlannedStartDate())) {
                throw new InputValidationException("Planned end date cannot be before planned start date");
            }
        }
        // Validate state and handle state transitions
        if (taskUpdateDto.getState() != null) {
            TaskStateEnum newState = taskUpdateDto.getState();
            TaskStateEnum currentState = taskEntity.getState();
            // Handle state transitions
            if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.IN_PROGRESS) {
                taskEntity.setStartDate(Instant.now()); // Set startDate to current date
            } else if (currentState == TaskStateEnum.IN_PROGRESS && newState == TaskStateEnum.FINISHED) {
                taskEntity.setEndDate(Instant.now()); // Set endDate to current date
                long duration = ChronoUnit.DAYS.between(taskEntity.getStartDate(), taskEntity.getEndDate());
                taskEntity.setDuration(duration);
            }
            taskEntity.setState(newState);
        }
        taskEntity.setDescription(taskUpdateDto.getDescription());
        taskEntity.setPlannedStartDate(taskUpdateDto.getPlannedStartDate());
        taskEntity.setPlannedEndDate(taskUpdateDto.getPlannedEndDate());
    }


    public TaskGetDto convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        TaskGetDto taskGetDto = new TaskGetDto();
        taskGetDto.setId(taskEntity.getId());
        taskGetDto.setTitle(taskEntity.getTitle());
        taskGetDto.setDescription(taskEntity.getDescription());
        taskGetDto.setCreationDate(taskEntity.getCreationDate());
        taskGetDto.setPlannedStartDate(taskEntity.getPlannedStartDate());
        taskGetDto.setStartDate(taskEntity.getStartDate());
        taskGetDto.setPlannedEndDate(taskEntity.getPlannedEndDate());
        taskGetDto.setEndDate(taskEntity.getEndDate());
        taskGetDto.setDuration(taskEntity.getDuration());
        taskGetDto.setState(taskEntity.getState());
        taskGetDto.setResponsibleId(taskEntity.getResponsibleUser().getId());
        taskGetDto.setNonRegisteredExecutors(taskEntity.getAdditionalExecutors());
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