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
        if (taskCreateDto ==null){
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

    public void addUserTask(TaskAddUsersDto taskAddUsersDto) {

        // Fetch additional executers by their IDs
//        Set<UserEntity> registeredExecuters = new HashSet<>();
//        for (UsernameDto user : taskCreateDto.getRegisteredExecutors()) {
//            UserEntity additionalExecuter = userDao.findUserById(user.getId());
//            if (additionalExecuter == null) {
//                throw new EntityNotFoundException("Additional executer not found");
//            }
//            registeredExecuters.add(additionalExecuter);
//        }

    }

    public void addDependencyTask(TaskAddDependencyDto addDependencyDto) {

        // Fetch dependent tasks by their IDs
//        Set<TaskEntity> dependentTasks = new HashSet<>();
//        for (Long taskId : taskCreateDto.getDependentTasks()) {
//            TaskEntity dependentTask = taskDao.findTaskById(taskId);
//            if (dependentTask == null) {
//                throw new EntityNotFoundException("Dependent task not found");
//            }
//            dependentTasks.add(dependentTask);
//        }

    }

    public void updateTask(TaskUpdateDto taskUpdateDto) throws InputValidationException, EntityNotFoundException {
        if (taskUpdateDto == null){
            throw new InputValidationException("Invalid Dto");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskUpdateDto.getTaskId());
        if (taskEntity==null){
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
        taskGetDto.setDuration(taskEntity.getDuration());
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