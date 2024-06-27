package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dao.TaskDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Task.TaskAddDependencyDto;
import aor.fpbackend.dto.Task.TaskDetailedUpdateDto;
import aor.fpbackend.dto.Task.TaskGetDto;
import aor.fpbackend.dto.Task.TaskUpdateDto;
import aor.fpbackend.dto.User.UserBasicInfoDto;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.TaskEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.LogTypeEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.TaskStateEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;

import java.net.UnknownHostException;
import java.time.temporal.ChronoUnit;

import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * TaskBean is a stateless EJB that manages the operations related to tasks within a project.
 * It interacts with various DAOs to perform CRUD operations on task entities, user entities,
 * and project entities. This bean handles the addition, update, deletion, and dependency
 * management of tasks.
 * <p>
 * The class also performs input validation, logging, and ensures that transactions are handled
 * appropriately.
 * </p>
 * <p>
 * Technologies Used:
 * <ul>
 *     <li><b>Jakarta EE</b>: For dependency injection.</li>
 *     <li><b>SLF4J</b>: For logging operations.</li>
 * </ul>
 * </p>
 * <p>
 * Dependencies are injected using the {@link EJB} annotation, which includes DAOs for user,
 * task, and project entities. The bean also uses utility classes for logging and input validation.
 * </p>
 */
@Stateless
public class TaskBean implements Serializable {
    @EJB
    TaskDao taskDao;
    @EJB
    UserBean userBean;
    @EJB
    ProjectDao projectDao;
    @EJB
    UserDao userDao;
    @EJB
    ProjectBean projectBean;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    NotificationBean notificationBean;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TaskBean.class);


    /**
     * Retrieves a list of all tasks from the system.
     * <p>
     * This method fetches all task entities from the database and converts them
     * into a list of {@link TaskGetDto} objects for easier handling and presentation
     * in the application layer.
     * </p>
     *
     * @return a list of {@link TaskGetDto} representing all tasks in the system.
     */
    public List<TaskGetDto> getTasks() {
        try {
            List<TaskGetDto> tasks = convertTaskEntityListToTaskDtoList(taskDao.findAllTasks());
            LOGGER.info("Successfully fetched {} tasks", tasks.size());
            return tasks;
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of tasks associated with a specific project.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the provided project ID to ensure it is positive.</li>
     *     <li>Checks if the project exists in the database.</li>
     *     <li>Fetches the list of tasks associated with the project ID if the project exists.</li>
     *     <li>Converts the list of task entities to a list of TaskGetDto objects.</li>
     *     <li>Logs the number of tasks fetched for monitoring purposes.</li>
     * </ul>
     * </p>
     * <p>
     * ThreadContext is cleared at the end of the method execution to ensure no residual data remains.
     * </p>
     *
     * @param projectId the ID of the project whose tasks are to be retrieved.
     * @return a list of TaskGetDto objects representing the tasks associated with the specified project.
     * @throws EntityNotFoundException if the project ID is invalid or the project does not exist.
     */
    public List<TaskGetDto> getTasksByProject(long projectId) throws EntityNotFoundException {
        if(projectId < 1) {
            throw new EntityNotFoundException("Project ID cannot be negative");
        }
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        try {
            List<TaskGetDto> tasks = convertTaskEntityListToTaskDtoList(taskDao.getTasksByProjectId(projectId));
            LOGGER.info("Successfully fetched {} tasks for project ID: {}", tasks.size(), projectId);
            return tasks;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Retrieves the details of a specific task by its ID.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the provided task ID to ensure it is positive.</li>
     *     <li>Checks if the task exists in the database.</li>
     *     <li>Fetches the task entity associated with the task ID if the task exists.</li>
     *     <li>Converts the task entity to a TaskGetDto object.</li>
     *     <li>Logs the successful retrieval of the task for monitoring purposes.</li>
     * </ul>
     * </p>
     * <p>
     * ThreadContext is cleared at the end of the method execution to ensure no residual data remains.
     * </p>
     *
     * @param taskId the ID of the task to be retrieved.
     * @return a TaskGetDto object representing the details of the specified task.
     * @throws EntityNotFoundException if the task ID is invalid or the task does not exist.
     */
    public TaskGetDto getTasksById(long taskId) throws EntityNotFoundException {
        if(taskId < 1) {
            throw new EntityNotFoundException("Task ID cannot be negative");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskId);
        if (taskEntity == null) {
            LOGGER.warn("Task with ID: {} not found", taskId);
            throw new EntityNotFoundException("Task not found");
        }
        try {
            TaskGetDto taskGetDto = convertTaskEntityToTaskDto(taskEntity);
            LOGGER.info("Successfully fetched task with ID: {}", taskId);
            return taskGetDto;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Adds a new task to the specified project with the provided details.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the existence of the project and the responsible user.</li>
     *     <li>Validates the planned start and end dates.</li>
     *     <li>Creates and persists a new task entity associated with the project and responsible user.</li>
     *     <li>Creates a notification for the user marked as responsible for the new task.</li>
     * </ul>
     * </p>
     * <p>
     * ThreadContext is utilized to log user-specific information for auditing purposes.
     * </p>
     *
     * @param title the title of the task.
     * @param description the description of the task.
     * @param plannedStartDate the planned start date of the task.
     * @param plannedEndDate the planned end date of the task.
     * @param responsibleId the ID of the user responsible for the task.
     * @param projectId the ID of the project to which the task will be added.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws InputValidationException if the planned dates are invalid.
     * @throws UnknownHostException if there is an error with the host.
     */
    @Transactional
    public void addTask(String title, String description, Instant plannedStartDate, Instant plannedEndDate, long responsibleId, long projectId) throws EntityNotFoundException, InputValidationException, UnknownHostException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        UserEntity taskResponsible = userDao.findUserById(responsibleId);
        if (taskResponsible == null) {
            throw new EntityNotFoundException("User not found");
        }
        // Validate planned dates
        if (plannedEndDate.isBefore(plannedStartDate)) {
            throw new InputValidationException("Planned end date cannot be before planned start date");
        }
        long daysBetween = ChronoUnit.DAYS.between(plannedStartDate, plannedEndDate);
        if (daysBetween < 1) {
            throw new InputValidationException("Planned end date must be at least one day after planned start date");
        }

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTitle(title);
        taskEntity.setDescription(description);
        taskEntity.setPlannedStartDate(plannedStartDate);
        taskEntity.setCreationDate(Instant.now());
        taskEntity.setPlannedEndDate(plannedEndDate);
        taskEntity.setDeleted(false);
        taskEntity.setState(TaskStateEnum.PLANNED);
        taskEntity.setResponsibleUser(taskResponsible);
        taskEntity.setProject(projectEntity);

        try{
            taskDao.persist(taskEntity);
            taskResponsible.getResponsibleTasks().add(taskEntity);
            notificationBean.createNotificationMarkesAsResponsibleInNewTask(taskResponsible, taskEntity);
            LOGGER.info("Task entity persisted successfully: " + taskEntity.getTitle() + "on project: " + projectEntity.getName());
        } catch (PersistenceException e) {
            LOGGER.error("Error while persisting task entity: {}", e.getMessage());
            throw new EntityNotFoundException("Error while persisting task entity");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Adds a dependency between two tasks.
     * <p>
     * This method validates the existence of the main and dependent tasks,
     * and then defines a dependency relationship between them.
     * </p>
     *
     * @param addDependencyDto the DTO containing the IDs of the main and dependent tasks.
     * @throws EntityNotFoundException if either the main task or the dependent task is not found.
     */
    @Transactional
    public void addDependencyTask(TaskAddDependencyDto addDependencyDto) throws EntityNotFoundException {
        TaskEntity mainTaskEntity = taskDao.findTaskById(addDependencyDto.getMainTaskId());
        TaskEntity dependentTaskEntity = taskDao.findTaskById(addDependencyDto.getDependentTaskId());
        if (mainTaskEntity == null) {
            LOGGER.warn("Main task with ID: {} not found", addDependencyDto.getMainTaskId());
            throw new EntityNotFoundException("Main task not found");
        }
        if (dependentTaskEntity == null) {
            LOGGER.warn("Dependent task with ID: {} not found", addDependencyDto.getDependentTaskId());
            throw new EntityNotFoundException("Dependent task not found");
        }
        try{
            Set<TaskEntity> dependentTasks = mainTaskEntity.getDependentTasks();
            dependentTasks.add(dependentTaskEntity);
            Set<TaskEntity> prerequisites = dependentTaskEntity.getPrerequisites();
            prerequisites.add(mainTaskEntity);
            LOGGER.info("Dependency added successfully: Main Task ID: {}, Dependent Task ID: {}", addDependencyDto.getMainTaskId(), addDependencyDto.getDependentTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while adding dependency: {}", e.getMessage());
            throw new EntityNotFoundException("Error while adding dependency");
        } finally {
            ThreadContext.clearMap();
        }
    }


    @Transactional
    public void removeDependencyTask(TaskAddDependencyDto addDependencyDto) throws EntityNotFoundException {
        TaskEntity mainTaskEntity = taskDao.findTaskById(addDependencyDto.getMainTaskId());
        TaskEntity dependentTaskEntity = taskDao.findTaskById(addDependencyDto.getDependentTaskId());
        if (mainTaskEntity == null || dependentTaskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        removeDependency(mainTaskEntity, dependentTaskEntity);
    }

    private void removeDependency(TaskEntity mainTaskEntity, TaskEntity dependentTaskEntity) {
        // Remove dependent task from main task's dependentTasks
        Set<TaskEntity> dependentTasks = mainTaskEntity.getDependentTasks();
        dependentTasks.remove(dependentTaskEntity);

        // Remove main task from dependent task's prerequisites
        Set<TaskEntity> prerequisites = dependentTaskEntity.getPrerequisites();
        prerequisites.remove(mainTaskEntity);
    }

    public void updateTask(TaskUpdateDto taskUpdateDto, SecurityContext securityContext) throws InputValidationException, EntityNotFoundException, UserNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        if (authUserEntity == null) {
            throw new UserNotFoundException("User not found with this Id");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskUpdateDto.getTaskId());
        if (taskEntity == null) {
            throw new EntityNotFoundException("Task not found with this Id");
        }
        EnumSet<ProjectStateEnum> dontUpdateStates = EnumSet.of(
                ProjectStateEnum.CANCELLED,
                ProjectStateEnum.READY,
                ProjectStateEnum.FINISHED);
        // Don't update tasks if project state is CANCELLED or FINISHED
        if (dontUpdateStates.contains(taskEntity.getProject().getState())) {
            throw new InputValidationException("Project state doesn't allow task updates");
        }
        // Validate planned dates
        if (taskUpdateDto.getPlannedEndDate().isBefore(taskUpdateDto.getPlannedStartDate())) {
            throw new InputValidationException("Planned end date cannot be before planned start date");
        }
        long daysBetween = ChronoUnit.DAYS.between(taskUpdateDto.getPlannedStartDate(), taskUpdateDto.getPlannedEndDate());
        if (daysBetween < 1) {
            throw new InputValidationException("Planned end date must be at least one day after planned start date");
        }
        taskEntity.setDescription(taskUpdateDto.getDescription());
        taskEntity.setPlannedStartDate(taskUpdateDto.getPlannedStartDate());
        taskEntity.setPlannedEndDate(taskUpdateDto.getPlannedEndDate());
        // Validate state and handle state transitions
        TaskStateEnum newState = taskUpdateDto.getState();
        TaskStateEnum currentState = taskEntity.getState();
        if (newState != currentState) {
            // Handle state transitions
            if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.IN_PROGRESS) {
                taskEntity.setStartDate(Instant.now()); // Set startDate to current date
            } else {
                if (currentState == TaskStateEnum.IN_PROGRESS && newState == TaskStateEnum.FINISHED) {
                    taskEntity.setEndDate(Instant.now()); // Set endDate to current date
                } else if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.FINISHED) {
                    taskEntity.setStartDate(Instant.now()); // Set startDate to current date
                    taskEntity.setEndDate(Instant.now()); // Set endDate to current date
                }
                // Calculate duration if end date is set
                if (taskEntity.getEndDate() != null) {
                    long duration = ChronoUnit.DAYS.between(taskEntity.getStartDate(), taskEntity.getEndDate());
                    taskEntity.setDuration(duration);
                }
            }
            taskEntity.setState(newState);
            String content = "Task state updated from " + currentState + " to " + newState + ", by " + authUserEntity.getUsername();
            projectBean.createProjectLog(taskEntity.getProject(), authUserEntity, LogTypeEnum.PROJECT_TASKS, content);
        }
    }

    @Transactional
    public void taskDetailedUpdate(TaskDetailedUpdateDto taskDetailedUpdateDto, SecurityContext securityContext) throws InputValidationException, EntityNotFoundException, UserNotFoundException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        if (authUserEntity == null) {
            throw new UserNotFoundException("User not found with this Id");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskDetailedUpdateDto.getTaskId());
        if (taskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        EnumSet<ProjectStateEnum> dontUpdateStates = EnumSet.of(
                ProjectStateEnum.CANCELLED,
                ProjectStateEnum.READY,
                ProjectStateEnum.FINISHED);
        // Don't update tasks if project state is CANCELLED or FINISHED
        if (dontUpdateStates.contains(taskEntity.getProject().getState())) {
            throw new InputValidationException("Project state doesn't allow task updates");
        }
        // Validate planned dates
        if (taskDetailedUpdateDto.getPlannedEndDate().isBefore(taskDetailedUpdateDto.getPlannedStartDate())) {
            throw new InputValidationException("Planned end date cannot be before planned start date");
        }
        long daysBetween = ChronoUnit.DAYS.between(taskDetailedUpdateDto.getPlannedStartDate(), taskDetailedUpdateDto.getPlannedEndDate());
        if (daysBetween < 1) {
            throw new InputValidationException("Planned end date must be at least one day after planned start date");
        }
        // Validate if new dates are compatible with dependencies and prerequisites
        for (TaskEntity dependentTask : taskEntity.getDependentTasks()) {
            if (dependentTask.getPlannedStartDate() != null && dependentTask.getPlannedStartDate().isBefore(taskDetailedUpdateDto.getPlannedStartDate())) {
                throw new InputValidationException("Planned start date is not compatible with dependent task: " + dependentTask.getTitle());
            }
        }
        for (TaskEntity prerequisite : taskEntity.getPrerequisites()) {
            if (prerequisite.getPlannedEndDate() != null && prerequisite.getPlannedEndDate().isAfter(taskDetailedUpdateDto.getPlannedStartDate())) {
                throw new InputValidationException("Planned start date is not compatible with prerequisite task: " + prerequisite.getTitle());
            }
        }
        if (taskDetailedUpdateDto.getPlannedEndDate() != null) {
            for (TaskEntity dependentTask : taskEntity.getDependentTasks()) {
                if (dependentTask.getPlannedEndDate().isBefore(taskDetailedUpdateDto.getPlannedEndDate())) {
                    throw new InputValidationException("Planned end date is not compatible with dependent task: " + dependentTask.getTitle());
                }
            }
            for (TaskEntity prerequisite : taskEntity.getPrerequisites()) {
                if (prerequisite.getPlannedStartDate().isAfter(taskDetailedUpdateDto.getPlannedEndDate())) {
                    throw new InputValidationException("Planned end date is not compatible with prerequisite task: " + prerequisite.getTitle());
                }
            }
        }
        // Update basic fields
        taskEntity.setTitle(taskDetailedUpdateDto.getTitle());
        taskEntity.setDescription(taskDetailedUpdateDto.getDescription());
        taskEntity.setPlannedStartDate(taskDetailedUpdateDto.getPlannedStartDate());
        taskEntity.setPlannedEndDate(taskDetailedUpdateDto.getPlannedEndDate());
        // Update responsible user
        UserEntity newResponsibleUser = userDao.findUserById(taskDetailedUpdateDto.getResponsibleUserId());
        if (newResponsibleUser == null) {
            throw new EntityNotFoundException("Responsible user not found");
        }
        if (!projectMemberDao.isUserProjectMember(taskEntity.getProject().getId(), newResponsibleUser.getId())) {
            throw new InputValidationException("Responsible user is not a member of the project");
        }
        taskEntity.setResponsibleUser(newResponsibleUser);
        if (taskEntity.getResponsibleUser().getId() != newResponsibleUser.getId()) {
            notificationBean.createNotificationMarkesAsResponsibleInNewTask(newResponsibleUser, taskEntity);
        }
        // Update registered executors
        Set<UserEntity> newRegisteredExecutors = new HashSet<>();
        for (Long executorId : taskDetailedUpdateDto.getRegisteredExecutors()) {
            UserEntity executor = userDao.findUserById(executorId);
            if (executor == null) {
                throw new EntityNotFoundException("Registered executor not found");
            }
            if (!projectMemberDao.isUserProjectMember(taskEntity.getProject().getId(), executorId)) {
                throw new InputValidationException("Executor with ID " + executorId + " is not a member of the project");
            }
            newRegisteredExecutors.add(executor);
            if (!taskEntity.getRegisteredExecutors().contains(executor)) {
                notificationBean.createNotificationMarkesAsExecutorInNewTask(executor, taskEntity);
            }
        }
        taskEntity.setRegisteredExecutors(newRegisteredExecutors);
        // Update non-registered executors
        taskEntity.setAdditionalExecutors(taskDetailedUpdateDto.getNonRegisteredExecutors());
        // Validate and update state
        TaskStateEnum newState = taskDetailedUpdateDto.getState();
        TaskStateEnum currentState = taskEntity.getState();
        if (newState != currentState) {
            if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.IN_PROGRESS) {
                taskEntity.setStartDate(Instant.now());
            } else if (currentState == TaskStateEnum.IN_PROGRESS && newState == TaskStateEnum.FINISHED) {
                taskEntity.setEndDate(Instant.now());
            } else if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.FINISHED) {
                taskEntity.setStartDate(Instant.now());
                taskEntity.setEndDate(Instant.now());
            }
            if (taskEntity.getEndDate() != null) {
                long duration = ChronoUnit.DAYS.between(taskEntity.getStartDate(), taskEntity.getEndDate());
                taskEntity.setDuration(duration);
            }
            taskEntity.setState(newState);
            String content = "Task state updated from " + currentState + " to " + newState + ", by " + authUserEntity.getUsername();
            projectBean.createProjectLog(taskEntity.getProject(), authUserEntity, LogTypeEnum.PROJECT_TASKS, content);
        }
    }

    public void deleteTask(long taskId, SecurityContext securityContext) throws EntityNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        if (authUserEntity == null) {
            throw new EntityNotFoundException("User not found with this Id");
        }
        TaskEntity taskEntity = taskDao.findTaskById(taskId);
        if (taskEntity == null) {
            throw new EntityNotFoundException("Task not found with this Id");
        }
        ProjectEntity projectEntity = taskEntity.getProject();
        boolean isProjectMember = projectMemberDao.isUserProjectMember(projectEntity.getId(), authUserEntity.getId());
        if (!isProjectMember) {
            throw new EntityNotFoundException("User is not a member of the project");
        }

        taskEntity.setDeleted(true);
        String content = "Task deleted by " + authUserEntity.getUsername();
        projectBean.createProjectLog(taskEntity.getProject(), authUserEntity, LogTypeEnum.PROJECT_TASKS, content);

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
        taskGetDto.setDeleted(taskEntity.isDeleted());
        taskGetDto.setResponsibleId(userBean.convertUserEntitytoUserBasicInfoDto(taskEntity.getResponsibleUser()));
        taskGetDto.setNonRegisteredExecutors(taskEntity.getAdditionalExecutors());
        taskGetDto.setProjectId(taskEntity.getProject().getId());
        // Map registered executors to UserBasicInfoDto set
        Set<UserBasicInfoDto> registeredExecutorsDtoSet = new HashSet<>();
        for (UserEntity user : taskEntity.getRegisteredExecutors()) {
            UserBasicInfoDto userDto = new UserBasicInfoDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setPhoto(user.getPhoto());
            userDto.setRole(user.getRole().getId());
            registeredExecutorsDtoSet.add(userDto);
        }
        taskGetDto.setRegisteredExecutors(registeredExecutorsDtoSet);
        // Map task IDs of prerequisites
        Set<Long> prerequisiteIds = new HashSet<>();
        for (TaskEntity prerequisite : taskEntity.getPrerequisites()) {
            prerequisiteIds.add(prerequisite.getId());
        }
        taskGetDto.setPrerequisites(prerequisiteIds);
        // Map task IDs of dependent tasks
        Set<Long> dependentTaskIds = new HashSet<>();
        for (TaskEntity dependentTask : taskEntity.getDependentTasks()) {
            dependentTaskIds.add(dependentTask.getId());
        }
        taskGetDto.setDependentTasks(dependentTaskIds);

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

    public List<TaskStateEnum> getEnumListTaskStates() {
        List<TaskStateEnum> taskStateEnums = new ArrayList<>();
        for (TaskStateEnum taskStateEnum : TaskStateEnum.values()) {
            taskStateEnums.add(taskStateEnum);
        }
        return taskStateEnums;
    }
}