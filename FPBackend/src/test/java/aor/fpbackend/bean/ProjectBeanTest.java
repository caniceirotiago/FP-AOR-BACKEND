package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Keyword.KeywordCreateNewProjectDto;
import aor.fpbackend.dto.Project.ProjectCreateDto;
import aor.fpbackend.dto.Project.ProjectGetDto;
import aor.fpbackend.dto.Project.ProjectUpdateDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.*;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectBeanTest {

    @InjectMocks
    private ProjectBean projectBean;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private LaboratoryDao labDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ProjectLogDao projectLogDao;

    @Mock
    private ProjectMembershipDao projectMemberDao;

    @Mock
    private UserBean userBean;

    @Mock
    private SkillBean skillBean;

    @Mock
    private KeywordBean keywordBean;

    @Mock
    private AssetBean assetBean;

    @Mock
    private LaboratoryBean laboratoryBean;

    @Mock
    private TaskBean taskBean;

    @Mock
    private MembershipBean memberBean;

    @Mock
    private NotificationBean notificationBean;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProject_Success() throws Exception {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName("Test Project");
        projectCreateDto.setConclusionDate(Instant.now().plusSeconds(3600));
        projectCreateDto.setKeywords(Set.of(new KeywordCreateNewProjectDto("keyword")));
        projectCreateDto.setLaboratoryId(1L);  // Adicionando o laboratório

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);
        when(projectDao.checkProjectNameExist("Test Project")).thenReturn(false);
        doAnswer(invocation -> {
            ProjectEntity projectEntity = invocation.getArgument(0);
            projectEntity.setId(1L);
            return null;
        }).when(projectDao).persist(any(ProjectEntity.class));
        when(projectDao.findProjectByName("Test Project")).thenReturn(new ProjectEntity());

        assertDoesNotThrow(() -> projectBean.createProject(projectCreateDto, securityContext));
        verify(projectDao, times(1)).persist(any(ProjectEntity.class));
    }


    @Test
    void testCreateProject_DuplicatedName() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName("Test Project");
        projectCreateDto.setConclusionDate(Instant.now().plusSeconds(3600));
        projectCreateDto.setKeywords(Set.of(new KeywordCreateNewProjectDto("keyword")));
        projectCreateDto.setLaboratoryId(1L);  // Set the laboratory ID

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);  // Ensure lab is found
        when(projectDao.checkProjectNameExist("Test Project")).thenReturn(true);

        assertThrows(InputValidationException.class, () -> projectBean.createProject(projectCreateDto, securityContext));
    }


    @Test
    void testGetAllProjects_Success() {
        List<ProjectEntity> projectEntities = List.of(new ProjectEntity());
        when(projectDao.findAllProjects()).thenReturn(new ArrayList<>(projectEntities));

        List<ProjectGetDto> projectGetDtos = projectBean.getAllProjects();
        assertEquals(1, projectGetDtos.size());
    }

    @Test
    void testGetProjectDetailsById_Success() throws EntityNotFoundException {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);

        ProjectGetDto projectGetDto = projectBean.getProjectDetailsById(1L);
        assertEquals(1L, projectGetDto.getId());
    }

    @Test
    void testGetProjectDetailsById_NotFound() {
        when(projectDao.findProjectById(1L)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> projectBean.getProjectDetailsById(1L));
    }

    @Test
    void testGetEnumListProjectStates_Success() {
        List<ProjectStateEnum> projectStateEnums = projectBean.getEnumListProjectStates();
        assertEquals(ProjectStateEnum.values().length, projectStateEnums.size());
    }

    @Test
    void testGetEnumListProjectRoles_Success() {
        List<ProjectRoleEnum> projectRoleEnums = projectBean.getEnumListProjectRoles();
        assertEquals(ProjectRoleEnum.values().length, projectRoleEnums.size());
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto();
        projectUpdateDto.setName("Updated Project");
        projectUpdateDto.setConclusionDate(Instant.now().plusSeconds(3600));
        projectUpdateDto.setLaboratoryId(1L);
        projectUpdateDto.setState(ProjectStateEnum.PLANNING);

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setName("Old Project Name");
        projectEntity.setState(ProjectStateEnum.PLANNING);

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);
        projectEntity.setLaboratory(labEntity);  // Set the laboratory

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);
        when(projectDao.checkProjectNameExist("Updated Project")).thenReturn(false);

        assertDoesNotThrow(() -> projectBean.updateProject(1L, projectUpdateDto, securityContext));
    }




    @Test
    void testUpdateProject_DuplicatedName() {
        ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto();
        projectUpdateDto.setName("Updated Project");
        projectUpdateDto.setConclusionDate(Instant.now().plusSeconds(3600));
        projectUpdateDto.setLaboratoryId(1L);
        projectUpdateDto.setState(ProjectStateEnum.PLANNING);

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setName("Old Project Name"); // Definindo o nome antigo do projeto
        projectEntity.setState(ProjectStateEnum.PLANNING);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(projectDao.checkProjectNameExist("Updated Project")).thenReturn(true);

        assertThrows(InputValidationException.class, () -> projectBean.updateProject(1L, projectUpdateDto, securityContext));
    }


    @Test
    void testUpdateProject_NotFound() {
        ProjectUpdateDto projectUpdateDto = new ProjectUpdateDto();
        projectUpdateDto.setName("Updated Project");

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(projectDao.findProjectById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> projectBean.updateProject(1L, projectUpdateDto, securityContext));
    }

}
