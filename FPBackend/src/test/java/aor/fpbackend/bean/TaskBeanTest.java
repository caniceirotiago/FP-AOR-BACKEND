package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dao.TaskDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Task.TaskDependencyDto;
import aor.fpbackend.dto.Task.TaskGetDto;
import aor.fpbackend.dto.Task.TaskUpdateDto;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.TaskEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.TaskStateEnum;
import aor.fpbackend.exception.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskBeanTest {

    @InjectMocks
    private TaskBean taskBean;
    @Mock
    private TaskDao taskDao;
    @Mock
    private ProjectDao projectDao;
    @Mock
    private UserDao userDao;
    @Mock
    private UserBean userBean;
    @Mock
    private ProjectMembershipDao projectMemberDao;
    @Mock
    private NotificationBean notificationBean;
    @Mock
    private ProjectBean projectBean;
    @Mock
    private ProjectEntity projectEntity;
    @Mock
    private UserEntity userEntity;
    @Mock
    private TaskEntity taskEntity;
    @Mock
    private TaskEntity mainTaskEntity;
    @Mock
    private TaskEntity dependentTaskEntity;
    @Mock
    private TaskDependencyDto taskDependencyDto;
    @Mock
    private TaskUpdateDto taskUpdateDto;
    @Mock
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

        taskDependencyDto = new TaskDependencyDto();
        taskDependencyDto.setMainTaskId(1L);
        taskDependencyDto.setDependentTaskId(2L);

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
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
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
    void testAddTask_Valid() throws EntityNotFoundException, InputValidationException, UnknownHostException, ElementAssociationException {
        long projectId = 1L;
        long responsibleId = 1L;
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(userDao.findUserById(responsibleId)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(projectId, responsibleId)).thenReturn(true);

        taskBean.addTask("Title", "Description", Instant.now(), Instant.now().plusSeconds(86400), responsibleId, projectId);

        verify(taskDao).persist(any(TaskEntity.class));
        verify(notificationBean).createNotificationMarksAsResponsibleInNewTask(any(UserEntity.class), any(TaskEntity.class));
    }

    @Test
    void testAddTask_InvalidProject() {
        long projectId = 1L;
        when(projectDao.findProjectById(projectId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> taskBean.addTask("Title", "Description", Instant.now(), Instant.now().plusSeconds(86400), 1L, projectId));
    }

    @Test
    void testAddDependencyTask_Valid() throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        long projectId = 1L;

        // Setup ProjectEntity
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);

        // Setup mainTaskEntity and dependentTaskEntity with ProjectEntity
        TaskEntity mainTaskEntity = new TaskEntity();
        mainTaskEntity.setId(taskDependencyDto.getMainTaskId());
        mainTaskEntity.setProject(projectEntity);
        mainTaskEntity.setPlannedEndDate(Instant.now().plusSeconds(3600));

        TaskEntity dependentTaskEntity = new TaskEntity();
        dependentTaskEntity.setId(taskDependencyDto.getDependentTaskId());
        dependentTaskEntity.setProject(projectEntity);
        dependentTaskEntity.setPlannedStartDate(Instant.now().plusSeconds(7200));

        // Mock DAO methods
        when(taskDao.findTaskById(taskDependencyDto.getMainTaskId())).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(taskDependencyDto.getDependentTaskId())).thenReturn(dependentTaskEntity);

        // Call the method under test
        taskBean.addDependencyTask(projectId, taskDependencyDto);

        // Verify the interactions with DAO
        verify(taskDao).findTaskById(taskDependencyDto.getMainTaskId());
        verify(taskDao).findTaskById(taskDependencyDto.getDependentTaskId());
    }



    @Test
    void testAddDependencyTask_MainTaskNotFound() {
        long projectId = 1L;
        when(taskDao.findTaskById(taskDependencyDto.getMainTaskId())).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> taskBean.addDependencyTask(projectId, taskDependencyDto));
    }

    @Test
    void testGetTasksById_ValidTaskId() throws EntityNotFoundException {
        long taskId = 1L;
        when(taskDao.findTaskById(taskId)).thenReturn(taskEntity);

        TaskGetDto result = taskBean.getTasksById(taskId);

        assertNotNull(result);
        verify(taskDao).findTaskById(taskId);
    }

    @Test
    void testGetTasksById_InvalidTaskId() {
        long taskId = -1L;
        assertThrows(EntityNotFoundException.class, () -> taskBean.getTasksById(taskId));
    }

    @Test
    void testGetTasksById_TaskNotFound() {
        long taskId = 1L;
        when(taskDao.findTaskById(taskId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> taskBean.getTasksById(taskId));
        verify(taskDao).findTaskById(taskId);
    }

    @Test
    void testAddTask_Success() throws EntityNotFoundException, InputValidationException, UnknownHostException, ElementAssociationException {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(true);

        taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);

        verify(taskDao, times(1)).persist(any(TaskEntity.class));
        verify(notificationBean, times(1)).createNotificationMarksAsResponsibleInNewTask(any(UserEntity.class), any(TaskEntity.class));
    }

    @Test
    void testAddTask_ProjectNotFound() {
        when(projectDao.findProjectById(1L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void testAddTask_UserNotFound() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testAddTask_UserNotProjectMember() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(false);

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), 1L, 1L);
        });

        assertEquals("Responsible user is not a member of the project", exception.getMessage());
    }

    @Test
    void testAddTask_InvalidDates() {
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectMemberDao.isUserProjectMember(1L, 1L)).thenReturn(true);

        InputValidationException exception = assertThrows(InputValidationException.class, () -> {
            taskBean.addTask("Test Task", "Task Description", Instant.now().plus(2, ChronoUnit.DAYS), Instant.now(), 1L, 1L);
        });

        assertEquals("Planned end date cannot be before planned start date", exception.getMessage());
    }

    @Test
    void testAddTask_PersistenceException() {
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
    void testAddDependencyTask_Success() throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        // Create a ProjectEntity
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);

        // Create and set up mainTaskEntity
        TaskEntity mainTaskEntity = new TaskEntity();
        mainTaskEntity.setId(1L);
        mainTaskEntity.setProject(projectEntity);
        mainTaskEntity.setPlannedEndDate(Instant.now().plusSeconds(3600)); // Set a valid planned end date

        // Create and set up dependentTaskEntity
        TaskEntity dependentTaskEntity = new TaskEntity();
        dependentTaskEntity.setId(2L);
        dependentTaskEntity.setProject(projectEntity);
        dependentTaskEntity.setPlannedStartDate(Instant.now().plusSeconds(7200)); // Set a valid planned start date

        // Create a TaskDependencyDto
        TaskDependencyDto taskDependencyDto = new TaskDependencyDto();
        taskDependencyDto.setMainTaskId(1L);
        taskDependencyDto.setDependentTaskId(2L);

        // Mock DAO methods
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(dependentTaskEntity);

        // Call the method under test
        taskBean.addDependencyTask(1L, taskDependencyDto);

        // Assert that the dependency relationship has been set correctly
        assertTrue(mainTaskEntity.getDependentTasks().contains(dependentTaskEntity));
        assertTrue(dependentTaskEntity.getPrerequisites().contains(mainTaskEntity));

        // Verify the interactions with the DAO
        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }


    @Test
    void testAddDependencyTask_DependentTaskNotFound() {
        when(taskDao.findTaskById(1L)).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(2L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.addDependencyTask(1L, taskDependencyDto);
        });

        assertEquals("Dependent task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(1L);
        verify(taskDao, times(1)).findTaskById(2L);
    }

    @Test
    void testRemoveDependencyTask_MainTaskNotFound() {
        long projectId = 1L;
        when(taskDao.findTaskById(taskDependencyDto.getMainTaskId())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.removeDependencyTask(projectId, taskDependencyDto);
        });

        assertEquals("Task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(taskDependencyDto.getMainTaskId());
        verify(taskDao, times(0)).findTaskById(taskDependencyDto.getDependentTaskId());
    }

    @Test
    void testRemoveDependencyTask_DependentTaskNotFound() {
        long projectId = 1L;
        when(taskDao.findTaskById(taskDependencyDto.getMainTaskId())).thenReturn(mainTaskEntity);
        when(taskDao.findTaskById(taskDependencyDto.getDependentTaskId())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBean.removeDependencyTask(projectId, taskDependencyDto);
        });

        assertEquals("Task not found", exception.getMessage());

        verify(taskDao, times(1)).findTaskById(taskDependencyDto.getMainTaskId());
        verify(taskDao, times(1)).findTaskById(taskDependencyDto.getDependentTaskId());
    }



    @Test
    void testUpdateTask_Success() throws InputValidationException, EntityNotFoundException, UserNotFoundException {
        // Configurar os mocks para retornar valores esperados
        when(userDao.findUserById(userEntity.getId())).thenReturn(userEntity);
        when(taskDao.findTaskById(taskUpdateDto.getTaskId())).thenReturn(taskEntity);

        // Chamar o método que está sendo testado
        taskBean.updateTask(taskUpdateDto, securityContext);

        // Verificar se taskDao.persist foi chamado com a entidade de tarefa correta
        verify(taskDao).persist(taskEntity);

        // Verificar se os campos foram atualizados corretamente
        assertEquals("Updated Description", taskEntity.getDescription());
        assertEquals(taskUpdateDto.getPlannedStartDate(), taskEntity.getPlannedStartDate());
        assertEquals(taskUpdateDto.getPlannedEndDate(), taskEntity.getPlannedEndDate());
        assertEquals(TaskStateEnum.IN_PROGRESS, taskEntity.getState());
        assertNotNull(taskEntity.getStartDate());
        assertNull(taskEntity.getEndDate()); // Verificar se endDate não foi definido, pois o estado é IN_PROGRESS
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
