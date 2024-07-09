package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dao.TaskDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Task.TaskDependencyDto;
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
import aor.fpbackend.exception.*;

import java.net.UnknownHostException;
import java.time.temporal.ChronoUnit;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
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
 * <p>
 * Technologies Used:
 * <ul>
 *     <li>Jakarta EE: For dependency injection.</li>
 *     <li>SLF4J: For logging operations.</li>
 * </ul>
 * <p>
 * Dependencies are injected using the {@link EJB} annotation, which includes DAOs for user,
 * task, and project entities. The bean also uses utility classes for logging and input validation.
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

//TODO acho que não está a ser utilizado
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
//    public List<TaskGetDto> getTasks() {
//        try {
//            List<TaskGetDto> tasks = convertTaskEntityListToTaskDtoList(taskDao.findAllTasks());
//            LOGGER.info("Successfully fetched {} tasks", tasks.size());
//            return tasks;
//        } finally {
//            ThreadContext.clearMap();
//        }
//    }


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
        if (projectId < 1) {
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
        if (taskId < 1) {
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
     * @param title            the title of the task.
     * @param description      the description of the task.
     * @param plannedStartDate the planned start date of the task.
     * @param plannedEndDate   the planned end date of the task.
     * @param responsibleId    the ID of the user responsible for the task.
     * @param projectId        the ID of the project to which the task will be added.
     * @throws EntityNotFoundException  if the project or user is not found.
     * @throws InputValidationException if the planned dates are invalid.
     * @throws UnknownHostException     if there is an error with the host.
     */
    @Transactional
    public void addTask(String title, String description, Instant plannedStartDate, Instant plannedEndDate, long responsibleId, long projectId) throws EntityNotFoundException, InputValidationException, UnknownHostException, ElementAssociationException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Don't add to CANCELLED or FINISHED projects
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState == ProjectStateEnum.CANCELLED || currentState == ProjectStateEnum.FINISHED) {
            throw new ElementAssociationException("Project is not editable anymore");
        }
        UserEntity taskResponsible = userDao.findUserById(responsibleId);
        if (taskResponsible == null) {
            throw new EntityNotFoundException("User not found");
        }
        // Validate that user is project member
        if (!projectMemberDao.isUserProjectMember(projectEntity.getId(), taskResponsible.getId())) {
            throw new InputValidationException("Responsible user is not a member of the project");
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

        try {
            taskDao.persist(taskEntity);
            taskResponsible.getResponsibleTasks().add(taskEntity);
            notificationBean.createNotificationMarksAsResponsibleInNewTask(taskResponsible, taskEntity);
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
     * @param dependencyDto the DTO containing the IDs of the main and dependent tasks.
     * @throws EntityNotFoundException if either the main task or the dependent task is not found.
     */
    @Transactional
    public void addDependencyTask(long projectId, TaskDependencyDto dependencyDto) throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        TaskEntity mainTaskEntity = taskDao.findTaskById(dependencyDto.getMainTaskId());
        if (mainTaskEntity == null) {
            throw new EntityNotFoundException("Main task not found");
        }
        TaskEntity dependentTaskEntity = taskDao.findTaskById(dependencyDto.getDependentTaskId());
        if (dependentTaskEntity == null) {
            throw new EntityNotFoundException("Dependent task not found");
        }
        if (mainTaskEntity.isDeleted() || dependentTaskEntity.isDeleted()) {
            throw new InputValidationException("Cannot use deleted tasks");
        }
        if (mainTaskEntity.getProject().getId() != projectId || dependentTaskEntity.getProject().getId() != projectId) {
            throw new InputValidationException("Tasks don't belong to this project");
        }
        if (dependentTaskEntity.getPlannedStartDate().isBefore(mainTaskEntity.getPlannedEndDate())) {
            throw new InputValidationException("Task is not compatible for dependent task");
        }
        // Check if the dependency already exists
        if (mainTaskEntity.getDependentTasks().contains(dependentTaskEntity)) {
            throw new InputValidationException("Dependency already exists");
        }
        try {
            Set<TaskEntity> dependentTasks = mainTaskEntity.getDependentTasks();
            dependentTasks.add(dependentTaskEntity);
            Set<TaskEntity> prerequisites = dependentTaskEntity.getPrerequisites();
            prerequisites.add(mainTaskEntity);
            LOGGER.info("Dependency added successfully: Main Task ID: {}, Dependent Task ID: {}", dependencyDto.getMainTaskId(), dependencyDto.getDependentTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while adding dependency: {}", e.getMessage());
            throw new DatabaseOperationException("Error while adding dependency");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Removes a dependency between two tasks.
     * <p>
     * This method validates the existence of the main and dependent tasks,
     * and then removes the dependency relationship between them.
     * </p>
     *
     * @param dependencyDto the DTO containing the IDs of the main and dependent tasks.
     * @throws EntityNotFoundException    if either the main task or the dependent task is not found.
     * @throws DatabaseOperationException if there is an error while removing the dependency.
     */
    @Transactional
    public void removeDependencyTask(long projectId, TaskDependencyDto dependencyDto) throws EntityNotFoundException, DatabaseOperationException, InputValidationException {
        TaskEntity mainTaskEntity = taskDao.findTaskById(dependencyDto.getMainTaskId());
        if (mainTaskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        TaskEntity dependentTaskEntity = taskDao.findTaskById(dependencyDto.getDependentTaskId());
        if (dependentTaskEntity == null) {
            throw new EntityNotFoundException("Task not found");
        }
        if (mainTaskEntity.isDeleted() || dependentTaskEntity.isDeleted()) {
            throw new InputValidationException("Cannot use deleted tasks");
        }
        if (mainTaskEntity.getProject().getId() != projectId || dependentTaskEntity.getProject().getId() != projectId) {
            throw new InputValidationException("Tasks don't belong to this project");
        }
        // Check if the dependency exists
        if (!mainTaskEntity.getDependentTasks().contains(dependentTaskEntity)) {
            throw new InputValidationException("Dependency doesn't exists");
        }
        try {
            Set<TaskEntity> dependentTasks = mainTaskEntity.getDependentTasks();
            dependentTasks.remove(dependentTaskEntity);
            Set<TaskEntity> prerequisites = dependentTaskEntity.getPrerequisites();
            prerequisites.remove(mainTaskEntity);
            LOGGER.info("Dependency removed successfully: Main Task ID: {}, Dependent Task ID:" +
                    " {}", dependencyDto.getMainTaskId(), dependencyDto.getDependentTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while removing dependency: {}", e.getMessage());
            throw new DatabaseOperationException("Error while removing dependency");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Updates the details of an existing task.
     * <p>
     * This method performs several validation checks, updates task details, and logs changes.
     * </p>
     *
     * @param taskUpdateDto   the DTO containing updated task details.
     * @param securityContext the security context of the authenticated user.
     * @throws InputValidationException if the task details are invalid.
     * @throws EntityNotFoundException  if the task or user is not found.
     * @throws UserNotFoundException    if the authenticated user is not found.
     */
    @Transactional
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
        if (taskEntity.isDeleted()) {
            throw new InputValidationException("Cannot update deleted tasks");
        }
        EnumSet<ProjectStateEnum> dontUpdateStates = EnumSet.of(
                ProjectStateEnum.CANCELLED,
                ProjectStateEnum.READY,
                ProjectStateEnum.FINISHED);
        // Don't update tasks if project state is CANCELLED, READY or FINISHED
        if (dontUpdateStates.contains(taskEntity.getProject().getState())) {
            throw new InputValidationException("Project state doesn't allow task updates");
        }
        // Validate planned dates
        validatePlannedDates(taskUpdateDto.getPlannedStartDate(), taskUpdateDto.getPlannedEndDate());
        try {
            taskEntity.setDescription(taskUpdateDto.getDescription());
            taskEntity.setPlannedStartDate(taskUpdateDto.getPlannedStartDate());
            taskEntity.setPlannedEndDate(taskUpdateDto.getPlannedEndDate());
            // Validate state and handle state transitions
            validateAndHandleStateTransition(taskEntity, taskUpdateDto.getState(), authUserEntity);
            taskDao.persist(taskEntity);
            LOGGER.info("Task updated successfully: Task ID: {}", taskUpdateDto.getTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while updating task: {}", e.getMessage());
            throw new EntityNotFoundException("Error while updating task");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Handles state transitions for a task entity based on the provided taskUpdate DTO.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the new state differs from the current state.</li>
     *     <li>Updates the task's start and end dates based on state transitions:
     *         <ul>
     *             <li>If transitioning from PLANNED to IN_PROGRESS, sets the start date to now.</li>
     *             <li>If transitioning from IN_PROGRESS to FINISHED, sets the end date to now.</li>
     *             <li>If transitioning directly from PLANNED to FINISHED, sets both start and end dates to now.</li>
     *         </ul>
     *     </li>
     *     <li>Calculates and sets the duration of the task if the end date is set.</li>
     *     <li>Updates the task's state to the new state.</li>
     *     <li>Logs the state transition in the project log.</li>
     * </ul>
     * </p>
     *
     * @param taskEntity     the task entity whose state is being updated.
     * @param newState       the new TaskStateEnum.
     * @param authUserEntity the authenticated user performing the update.
     */
    private void validateAndHandleStateTransition(TaskEntity taskEntity, TaskStateEnum newState, UserEntity authUserEntity) {
        TaskStateEnum currentState = taskEntity.getState();
        if (newState != currentState) {
            // Handle state transitions
            if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.IN_PROGRESS) {
                taskEntity.setStartDate(Instant.now()); // Set startDate to current date
            } else if (currentState == TaskStateEnum.IN_PROGRESS && newState == TaskStateEnum.FINISHED) {
                taskEntity.setEndDate(Instant.now()); // Set endDate to current date
            } else if (currentState == TaskStateEnum.PLANNED && newState == TaskStateEnum.FINISHED) {
                taskEntity.setStartDate(Instant.now()); // Set startDate to current date
                taskEntity.setEndDate(Instant.now()); // Set endDate to current date
            }
            // Handle transitions from FINISHED to other states
            if (currentState == TaskStateEnum.FINISHED && newState != TaskStateEnum.FINISHED) {
                taskEntity.setEndDate(null); // Clear endDate
                taskEntity.setDuration(null); // Clear duration
            }
            // Calculate duration if end date is set and state is FINISHED
            if (newState == TaskStateEnum.FINISHED && taskEntity.getEndDate() != null) {
                long duration = ChronoUnit.DAYS.between(taskEntity.getStartDate(), taskEntity.getEndDate());
                taskEntity.setDuration(duration);
            }
            taskEntity.setState(newState);
            String content = "Task state updated from " + currentState + " to " + newState + ", by " + authUserEntity.getUsername();
            projectBean.createProjectLog(taskEntity.getProject(), authUserEntity, LogTypeEnum.PROJECT_TASKS, content);
        }
    }

    /**
     * Updates the details of a task, including state transitions, dependencies, and notifications.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's entity from the database.</li>
     *     <li>Retrieves the task entity from the database using the provided task ID.</li>
     *     <li>Validates that the project state allows task updates.</li>
     *     <li>Validates the planned start and end dates.</li>
     *     <li>Validates dependencies and prerequisites for date compatibility.</li>
     *     <li>Validates the responsible user and registered executors.</li>
     *     <li>Updates the task entity fields with the provided details.</li>
     *     <li>Handles state transitions and logs the changes.</li>
     *     <li>Sends notifications to the responsible user and executors if there are changes.</li>
     * </ul>
     * </p>
     *
     * @param taskDetailedUpdateDto the DTO containing the detailed update information for the task.
     * @param securityContext       the security context containing the authenticated user's details.
     * @throws InputValidationException   if any validation of input data fails.
     * @throws EntityNotFoundException    if the task, responsible user, or registered executors are not found.
     * @throws UserNotFoundException      if the authenticated user is not found.
     * @throws UnknownHostException       if there is an error related to the host environment.
     * @throws DatabaseOperationException if there is an error updating the task in the database.
     */
    @Transactional
    public void taskDetailedUpdate(TaskDetailedUpdateDto taskDetailedUpdateDto, SecurityContext securityContext) throws InputValidationException, EntityNotFoundException, UserNotFoundException, UnknownHostException, DatabaseOperationException {
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
        if (dontUpdateStates.contains(taskEntity.getProject().getState())) {
            throw new InputValidationException("Project state doesn't allow task updates");
        }
        validatePlannedDates(taskDetailedUpdateDto.getPlannedStartDate(), taskDetailedUpdateDto.getPlannedEndDate());
        validateDependenciesAndPrerequisites(taskDetailedUpdateDto, taskEntity);
        UserEntity newResponsibleUser = validateResponsibleUser(taskDetailedUpdateDto, taskEntity);
        Set<UserEntity> newRegisteredExecutors = validateRegisteredExecutors(taskDetailedUpdateDto, taskEntity);
        try {
            updateTaskEntityFields(taskEntity, taskDetailedUpdateDto, newResponsibleUser, newRegisteredExecutors);
            validateAndHandleStateTransition(taskEntity, taskDetailedUpdateDto.getState(), authUserEntity);
            notifyChanges(taskEntity, newResponsibleUser, newRegisteredExecutors);
            LOGGER.info("Task updated successfully: Task ID: {}", taskDetailedUpdateDto.getTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while updating task: {}", e.getMessage());
            throw new DatabaseOperationException("Error while updating task");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Validates the planned start and end dates for a task update.
     * <p>
     * This method performs the following validations:
     * <ul>
     *     <li>Checks if the planned end date is before the planned start date.</li>
     *     <li>Ensures that the planned end date is at least one day after the planned start date.</li>
     * </ul>
     * If any of these validations fail, an InputValidationException is thrown.
     * </p>
     *
     * @param plannedStartDate the Instant containing the planned start date for the task.
     * @param plannedEndDate   the Instant containing the planned end date for the task.
     * @throws InputValidationException if the planned end date is before the planned start date or if the duration is less than one day.
     */
    private void validatePlannedDates(Instant plannedStartDate, Instant plannedEndDate) throws InputValidationException {
        if (plannedEndDate.isBefore(plannedStartDate)) {
            throw new InputValidationException("Planned end date cannot be before planned start date");
        }
        long daysBetween = ChronoUnit.DAYS.between(plannedStartDate, plannedEndDate);
        if (daysBetween < 1) {
            throw new InputValidationException("Planned end date must be at least one day after planned start date");
        }
    }


    /**
     * Validates the compatibility of new planned dates with the dependencies and prerequisites of a task.
     * <p>
     * This method performs the following validations:
     * <ul>
     *     <li>Ensures that the new planned start date is not before the planned start dates of any dependent tasks.</li>
     *     <li>Ensures that the new planned start date is not after the planned end dates of any prerequisite tasks.</li>
     *     <li>Ensures that the new planned end date is not before the planned end dates of any dependent tasks.</li>
     *     <li>Ensures that the new planned end date is not after the planned start dates of any prerequisite tasks.</li>
     * </ul>
     * If any of these validations fail, an InputValidationException is thrown.
     * </p>
     *
     * @param taskDetailedUpdateDto the DTO containing the new planned dates for the task.
     * @param taskEntity            the task entity to be updated, containing the current dependencies and prerequisites.
     * @throws InputValidationException if the new planned dates are not compatible with the dates of dependent or prerequisite tasks.
     */
    private void validateDependenciesAndPrerequisites(TaskDetailedUpdateDto taskDetailedUpdateDto, TaskEntity taskEntity) throws InputValidationException {
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
    }


    /**
     * Validates the new responsible user for a task.
     * <p>
     * This method performs the following validations:
     * <ul>
     *     <li>Ensures that the new responsible user exists in the database.</li>
     *     <li>Checks that the new responsible user is a member of the project associated with the task.</li>
     * </ul>
     * If any of these validations fail, an appropriate exception is thrown.
     * </p>
     *
     * @param taskDetailedUpdateDto the DTO containing the ID of the new responsible user.
     * @param taskEntity            the task entity to be updated, containing the current project information.
     * @return the UserEntity representing the new responsible user.
     * @throws EntityNotFoundException  if the responsible user is not found.
     * @throws InputValidationException if the responsible user is not a member of the project.
     */
    private UserEntity validateResponsibleUser(TaskDetailedUpdateDto taskDetailedUpdateDto, TaskEntity taskEntity) throws EntityNotFoundException, InputValidationException {
        UserEntity newResponsibleUser = userDao.findUserById(taskDetailedUpdateDto.getResponsibleUserId());
        if (newResponsibleUser == null) {
            throw new EntityNotFoundException("Responsible user not found");
        }
        if (!projectMemberDao.isUserProjectMember(taskEntity.getProject().getId(), newResponsibleUser.getId())) {
            throw new InputValidationException("Responsible user is not a member of the project");
        }
        return newResponsibleUser;
    }


    /**
     * Validates the registered executors for a task.
     * <p>
     * This method performs the following validations:
     * <ul>
     *     <li>Ensures that each registered executor exists in the database.</li>
     *     <li>Checks that each registered executor is a member of the project associated with the task.</li>
     * </ul>
     * If any of these validations fail, an appropriate exception is thrown.
     * </p>
     *
     * @param taskDetailedUpdateDto the DTO containing the list of registered executor IDs.
     * @param taskEntity            the task entity to be updated, containing the current project information.
     * @return a set of UserEntity objects representing the validated registered executors.
     * @throws EntityNotFoundException  if any registered executor is not found.
     * @throws InputValidationException if any registered executor is not a member of the project.
     */
    private Set<UserEntity> validateRegisteredExecutors(TaskDetailedUpdateDto taskDetailedUpdateDto, TaskEntity taskEntity) throws EntityNotFoundException, InputValidationException {
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
        }
        return newRegisteredExecutors;
    }

    /**
     * Updates the fields of a task entity with the values from a detailed update DTO.
     * <p>
     * This method updates various attributes of the task entity including:
     * <ul>
     *     <li>Responsible user</li>
     *     <li>Title</li>
     *     <li>Description</li>
     *     <li>Planned start and end dates</li>
     *     <li>Registered executors</li>
     *     <li>Additional executors</li>
     * </ul>
     * The method assumes that all necessary validations have already been performed before it is called.
     * </p>
     *
     * @param taskEntity             the task entity to be updated.
     * @param taskDetailedUpdateDto  the DTO containing the updated task details.
     * @param newResponsibleUser     the new responsible user for the task.
     * @param newRegisteredExecutors the set of new registered executors for the task.
     */
    private void updateTaskEntityFields(TaskEntity taskEntity, TaskDetailedUpdateDto taskDetailedUpdateDto, UserEntity newResponsibleUser, Set<UserEntity> newRegisteredExecutors) {
        taskEntity.setResponsibleUser(newResponsibleUser);
        taskEntity.setTitle(taskDetailedUpdateDto.getTitle());
        taskEntity.setDescription(taskDetailedUpdateDto.getDescription());
        taskEntity.setPlannedStartDate(taskDetailedUpdateDto.getPlannedStartDate());
        taskEntity.setPlannedEndDate(taskDetailedUpdateDto.getPlannedEndDate());
        taskEntity.setRegisteredExecutors(newRegisteredExecutors);
        taskEntity.setAdditionalExecutors(taskDetailedUpdateDto.getNonRegisteredExecutors());
    }


    /**
     * Sends notifications for changes in task responsibility and registered executors.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>If the responsible user for the task has changed, it sends a notification to the new responsible user.</li>
     *     <li>For each new registered executor, it checks if they were not previously registered and sends a notification.</li>
     * </ul>
     * </p>
     *
     * @param taskEntity             the task entity with updated details.
     * @param newResponsibleUser     the new responsible user for the task.
     * @param newRegisteredExecutors the set of new registered executors for the task.
     * @throws UnknownHostException if there is an error sending notifications due to network issues.
     */
    private void notifyChanges(TaskEntity taskEntity, UserEntity newResponsibleUser, Set<UserEntity> newRegisteredExecutors) throws UnknownHostException {
        if (taskEntity.getResponsibleUser().getId() != newResponsibleUser.getId()) {
            notificationBean.createNotificationMarksAsResponsibleInNewTask(newResponsibleUser, taskEntity);
        }
        for (UserEntity newExecutor : newRegisteredExecutors) {
            if (!taskEntity.getRegisteredExecutors().contains(newExecutor)) {
                notificationBean.createNotificationMarksAsExecutorInNewTask(newExecutor, taskEntity);
            }
        }
    }


    /**
     * Deletes a task identified by its ID.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Validates the existence of the user, task, and project membership.</li>
     *     <li>Ensures that all task dependencies are removed, both the dependent tasks and the prerequisites.</li>
     *     <li>Marks the task as deleted by setting the deleted flag to true.</li>
     *     <li>Logs the deletion action in the project logs for auditing purposes.</li>
     *     <li>Handles any persistence exceptions and logs errors that occur during the deletion process.</li>
     * </ul>
     * </p>
     *
     * @param taskId          the ID of the task to be deleted.
     * @param securityContext the security context containing the authenticated user's details.
     * @throws EntityNotFoundException if the user, task, or project membership is not found.
     */
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
        try {
            // Remove task dependencies
            for (TaskEntity dependentTask : taskEntity.getDependentTasks()) {
                dependentTask.getPrerequisites().remove(taskEntity);
            }
            taskEntity.getDependentTasks().clear();
            for (TaskEntity prerequisite : taskEntity.getPrerequisites()) {
                prerequisite.getDependentTasks().remove(taskEntity);
            }
            taskEntity.getPrerequisites().clear();
            // Mark the task as deleted
            taskEntity.setDeleted(true);
            String content = "Task deleted by " + authUserEntity.getUsername();
            projectBean.createProjectLog(taskEntity.getProject(), authUserEntity, LogTypeEnum.PROJECT_TASKS, content);
            LOGGER.info("Task deleted successfully: Task ID: {}", taskId);
        } catch (PersistenceException e) {
            LOGGER.error("Error while deleting task: {}", e.getMessage());
            throw new EntityNotFoundException("Error while deleting task");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Converts a TaskEntity object to a TaskGetDto object.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Maps basic fields such as id, title, description, creation date, planned start date, start date, planned end date, end date, duration, state, deleted status, and project ID from TaskEntity to TaskGetDto.</li>
     *     <li>Converts the responsible user from TaskEntity to UserBasicInfoDto using the userBean's conversion method.</li>
     *     <li>Maps the set of registered executors from TaskEntity to a set of UserBasicInfoDto.</li>
     *     <li>Maps the set of prerequisites and dependent tasks from TaskEntity to sets of task IDs.</li>
     * </ul>
     * </p>
     *
     * @param taskEntity the TaskEntity object to be converted.
     * @return a TaskGetDto object populated with the data from the TaskEntity.
     */
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


    /**
     * Converts a list of TaskEntity objects to a list of TaskGetDto objects.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Creates a new list to hold the converted TaskGetDto objects.</li>
     *     <li>Iterates over the provided list of TaskEntity objects.</li>
     *     <li>For each TaskEntity, converts it to a TaskGetDto using the convertTaskEntityToTaskDto method.</li>
     *     <li>Adds the converted TaskGetDto to the new list.</li>
     * </ul>
     * </p>
     *
     * @param taskEntities the list of TaskEntity objects to be converted.
     * @return a list of TaskGetDto objects populated with the data from the TaskEntity objects.
     */
    public List<TaskGetDto> convertTaskEntityListToTaskDtoList(List<TaskEntity> taskEntities) {
        List<TaskGetDto> taskGetDtos = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            TaskGetDto taskGetDto = convertTaskEntityToTaskDto(taskEntity);
            taskGetDtos.add(taskGetDto);
        }
        return taskGetDtos;
    }


    /**
     * Retrieves a list of all TaskStateEnum values.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Creates a new list to hold the TaskStateEnum values.</li>
     *     <li>Iterates over all values of the TaskStateEnum enumeration.</li>
     *     <li>Adds each TaskStateEnum value to the list.</li>
     * </ul>
     * </p>
     *
     * @return a list of TaskStateEnum values representing all possible states of a task.
     */
    public List<TaskStateEnum> getEnumListTaskStates() {
        List<TaskStateEnum> taskStateEnums = new ArrayList<>();
        for (TaskStateEnum taskStateEnum : TaskStateEnum.values()) {
            taskStateEnums.add(taskStateEnum);
        }
        return taskStateEnums;
    }


}