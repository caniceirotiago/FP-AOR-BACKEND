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
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.TaskEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.TaskStateEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskBeanTest {

    @Mock
    private TaskDao taskDao;

    @Mock
    private UserBean userBean;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ProjectBean projectBean;

    @Mock
    private ProjectMembershipDao projectMemberDao;

    @Mock
    private NotificationBean notificationBean;

    @InjectMocks
    private TaskBean taskBean;

    private ProjectEntity projectEntity;
    private UserEntity userEntity;
    private TaskEntity taskEntity;
    private TaskEntity mainTaskEntity;
    private TaskEntity dependentTaskEntity;
    private TaskAddDependencyDto taskAddDependencyDto;
    private TaskUpdateDto taskUpdateDto;
    private SecurityContext securityContext;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        projectEntity = new ProjectEntity();
        projectEntity.setId(1L);

        userEntity = new UserEntity();
        userEntity.setId(1L);

        taskEntity = new TaskEntity();
        taskEntity.setId(1L);
        taskEntity.setTitle("Test Task");
        taskEntity.setDescription("Task Description");
        taskEntity.setPlannedStartDate(Instant.now());
        taskEntity.setPlannedEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
        taskEntity.setState(TaskStateEnum.PLANNED);
        taskEntity.setResponsibleUser(userEntity);
        taskEntity.setProject(projectEntity);

        mainTaskEntity = new TaskEntity();
        mainTaskEntity.setId(1L);
        mainTaskEntity.setDependentTasks(new HashSet<>());

        dependentTaskEntity = new TaskEntity();
        dependentTaskEntity.setId(2L);
        dependentTaskEntity.setPrerequisites(new HashSet<>());

        taskAddDependencyDto = new TaskAddDependencyDto();
        taskAddDependencyDto.setMainTaskId(1L);
        taskAddDependencyDto.setDependentTaskId(2L);

        taskUpdateDto = new TaskUpdateDto();
        taskUpdateDto.setTaskId(1L);

        taskUpdateDto.setDescription("Updated Description");
        taskUpdateDto.setPlannedStartDate(Instant.now());
        taskUpdateDto.setPlannedEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
        taskUpdateDto.setState(TaskStateEnum.IN_PROGRESS);

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(userEntity.getId());

        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        when(userDao.findUserById(userEntity.getId())).thenReturn(userEntity);
        when(taskDao.findTaskById(taskUpdateDto.getTaskId())).thenReturn(taskEntity);
    }

    @Test
    void testGetTasksByProject_ValidProjectId() throws EntityNotFoundException {
        long projectId = 1L;
        ProjectEntity projectEntity = new ProjectEntity();
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        TaskEntity taskEntity = new TaskEntity();
        when(taskDao.getTasksByProjectId(projectId)).thenReturn(Collections.singletonList(taskEntity));

        List<TaskGetDto> tasks = taskBean.getTasksByProject(projectId);

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        verify(projectDao).findProjectById(projectId);
        verify(taskDao).getTasksByProjectId(projectId);
    }

    @Test
    void testGetTasksByProject_InvalidProjectId() {
        long projectId = -1L;

        assertThrows(EntityNotFoundException.class, () -> taskBean.getTasksByProject(projectId));
    }

    @Test
    void testAddTask_Valid() throws EntityNotFoundException, InputValidationException, UnknownHostException {
        long projectId = 1L;
        long responsibleId = 1L;
        ProjectEntity projectEntity = new ProjectEntity();
        UserEntity userEntity = new UserEntity();
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(userDao.findUserById(responsibleId)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(projectId, responsibleId)).thenReturn(true);

        taskBean.addTask("Title", "Description", Instant.now(), Instant.now().plusSeconds(86400), responsibleId, projectId);

        verify(taskDao).persist(any(TaskEntity.class));
        verify(notificationBean).createNotificationMarkesAsResponsibleInNewTask(any(UserEntity.class), any(TaskEntity.class));
    }

    @Test
    void testAddTask_InvalidProject() {
        long projectId = 1L;
        when(projectDao.findProjectById(projectId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> taskBean.addTask("Title", "Description", Instant.now(), Instant.now().plusSeconds(86400), 1L, projectId));
    }

    @Test
    void testAddDependencyTask_Valid() throws EntityNotFoundException {
        long mainTaskId = 1L;
        long dependentTaskId = 2L;
        TaskEntity mainTaskEntity = new TaskEntity();
        TaskEntity dependentTaskEntity = new TaskEntity();
        when(taskDao.findTaskById(mainTaskId)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(dependentTaskId)).thenReturn(dependentTaskEntity);

        TaskAddDependencyDto addDependencyDto = new TaskAddDependencyDto();
        addDependencyDto.setMainTaskId(mainTaskId);
        addDependencyDto.setDependentTaskId(dependentTaskId);

        taskBean.addDependencyTask(addDependencyDto);

        verify(taskDao).findTaskById(mainTaskId);
        verify(taskDao).findTaskById(dependentTaskId);
    }

    @Test
    void testAddDependencyTask_MainTaskNotFound() {
        long mainTaskId = 1L;
        long dependentTaskId = 2L;
        when(taskDao.findTaskById(mainTaskId)).thenReturn(null);

        TaskAddDependencyDto addDependencyDto = new TaskAddDependencyDto();
        addDependencyDto.setMainTaskId(mainTaskId);
        addDependencyDto.setDependentTaskId(dependentTaskId);

        assertThrows(EntityNotFoundException.class, () -> taskBean.addDependencyTask(addDependencyDto));
    }
    @Test
    void testGetTasksById_ValidTaskId() throws EntityNotFoundException {
        long taskId = 1L;
        TaskEntity taskEntity = new TaskEntity();
        when(taskDao.findTaskById(taskId)).thenReturn(taskEntity);
        TaskGetDto taskGetDto = new TaskGetDto();
        when(taskBean.convertTaskEntityToTaskDto(taskEntity)).thenReturn(taskGetDto);

        TaskGetDto result = taskBean.getTasksById(taskId);

        assertNotNull(result);
        verify(taskDao).findTaskById(taskId);
        verify(taskBean).convertTaskEntityToTaskDto(taskEntity);
    }

    @Test
    void testGetTasksById_InvalidTaskId() {
        long taskId = -1L;

        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.getTasksById(taskId);
        });

        String expectedMessage = "Task ID cannot be negative";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testGetTasksById_TaskNotFound() {
        long taskId = 1L;
        when(taskDao.findTaskById(taskId)).thenReturn(null);

        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.getTasksById(taskId);
        });

        String expectedMessage = "Task not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(taskDao).findTaskById(taskId);
    }
    @Test
    public void testAddTask_Success() throws EntityNotFoundException, InputValidationException, UnknownHostException {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(true);

        taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);

        verify(taskDao, times(1)).persist(any(TaskEntity.class));
        verify(notificationBean, times(1)).createNotificationMarkesAsResponsibleInNewTask(any(UserEntity.class), any(TaskEntity.class));
    }

    @Test
    public void testAddTask_ProjectNotFound() {
        when(projectDao.findProjectById(1L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    public void testAddTask_UserNotFound() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testAddTask_UserNotProjectMember() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(false);

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("Responsible user is not a member of the project", exception.getMessage());
    }

    @Test
    public void testAddTask_InvalidDates() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(true);

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now().plus(2, ChronoUnit.DAYS), Instant.now(), 1L, 1L);
        });

        assertEquals("Planned end date cannot be before planned start date", exception.getMessage());
    }

    @Test
    public void testAddTask_PersistenceException() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(true);
        doThrow(PersistenceException.class).when(taskDao).persist(any(TaskEntity.class));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("Error while persisting task entity", exception.getMessage());
    }
    @Test
    public void testAddDependencyTask_Success() throws EntityNotFoundException {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(dependentTaskEntity);

        taskBean.addDependencyTask(taskAddDependencyDto);

        assertTrue(mainTaskEntity.getDependentTasks().contains(dependentTaskEntity));
        assertTrue(dependentTaskEntity.getPrerequisites().contains(mainTaskEntity));

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }


    @Test
    public void testAddDependencyTask_DependentTaskNotFound() {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addDependencyTask(taskAddDependencyDto);
        });

        assertEquals("Dependent task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }

    @Test
    public void testAddDependencyTask_PersistenceException() {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(dependentTaskEntity);
        doThrow(PersistenceException.class).when(taskDao).persist(any(TaskEntity.class));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addDependencyTask(taskAddDependencyDto);
        });

        assertEquals("Error while adding dependency", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }
    @Test
    public void testRemoveDependencyTask_Success() throws EntityNotFoundException, DatabaseOperationException {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(dependentTaskEntity);

        taskBean.removeDependencyTask(taskAddDependencyDto);

        assertFalse(mainTaskEntity.getDependentTasks().contains(dependentTaskEntity));
        assertFalse(dependentTaskEntity.getPrerequisites().contains(mainTaskEntity));

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }

    @Test
    public void testRemoveDependencyTask_MainTaskNotFound() {
        when(taskDao.findTaskById(1L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.removeDependencyTask(taskAddDependencyDto);
        });

        assertEquals("Task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(0)).findTaskById(2L);
    }

    @Test
    public void testRemoveDependencyTask_DependentTaskNotFound() {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.removeDependencyTask(taskAddDependencyDto);
        });

        assertEquals("Task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }

    @Test
    public void testRemoveDependencyTask_PersistenceException() {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(dependentTaskEntity);
        doThrow(PersistenceException.class).when(taskDao).persist(any(TaskEntity.class));

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> {
            taskBean.removeDependencyTask(taskAddDependencyDto);
        });

        assertEquals("Error while removing dependency", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }

    @Test
    void testUpdateTask_Success() throws InputValidationException, EntityNotFoundException, UserNotFoundException {
        taskBean.updateTask(taskUpdateDto, securityContext);

        verify(taskDao).persist(taskEntity);
        assertEquals(TaskStateEnum.IN_PROGRESS, taskEntity.getState());
        assertNotNull(taskEntity.getStartDate());
    }

    @Test
    void testUpdateTask_UserNotFound() {
        when(userDao.findUserById(userEntity.getId())).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("User not found with this Id", exception.getMessage());
    }

    @Test
    void testUpdateTask_TaskNotFound() {
        when(taskDao.findTaskById(taskUpdateDto.getTaskId())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("Task not found with this Id", exception.getMessage());
    }

    @Test
    void testUpdateTask_ProjectStateNotAllowed() {
        projectEntity.setState(ProjectStateEnum.CANCELLED);

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("Project state doesn't allow task updates", exception.getMessage());
    }

    @Test
    void testUpdateTask_EndDateBeforeStartDate() {
        taskUpdateDto.setPlannedEndDate(taskUpdateDto.getPlannedStartDate().minus(1, ChronoUnit.DAYS));

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("Planned end date cannot be before planned start date", exception.getMessage());
    }

    @Test
    void testUpdateTask_EndDateLessThanOneDay() {
        taskUpdateDto.setPlannedEndDate(taskUpdateDto.getPlannedStartDate().plus(1, ChronoUnit.HOURS));

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("Planned end date must be at least one day after planned start date", exception.getMessage());
    }

    @Test
    void testUpdateTask_TransitionStatePlannedToFinished() throws InputValidationException, EntityNotFoundException, UserNotFoundException {
        taskUpdateDto.setState(TaskStateEnum.FINISHED);
        taskEntity.setState(TaskStateEnum.PLANNED);

        taskBean.updateTask(taskUpdateDto, securityContext);

        verify(taskDao).persist(taskEntity);
        assertEquals(TaskStateEnum.FINISHED, taskEntity.getState());
        assertNotNull(taskEntity.getStartDate());
        assertNotNull(taskEntity.getEndDate());
    }

    @Test
    void testUpdateTask_PersistenceException() {
        doThrow(PersistenceException.class).when(taskDao).persist(any(TaskEntity.class));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.updateTask(taskUpdateDto, securityContext);
        });

        assertEquals("Error while updating task", exception.getMessage());
    }
}
