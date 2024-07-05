package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.*;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MembershipBeanTest {

    @InjectMocks
    private MembershipBean membershipBean;
    @Mock
    private UserDao userDao;
    @Mock
    private ProjectDao projectDao;
    @Mock
    private ProjectMembershipDao projectMemberDao;
    @Mock
    private ConfigurationBean configurationBean;
    @Mock
    private NotificationBean notificationBean;
    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAskToJoinProject_Success() throws Exception {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        UserEntity userEntity = mock(UserEntity.class);
        ProjectEntity projectEntity = mock(ProjectEntity.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(userEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(projectEntity.getState()).thenReturn(ProjectStateEnum.PLANNING);
        when(configurationBean.getConfigValueByKey("maxProjectMembers")).thenReturn(10);
        when(projectEntity.getMembers()).thenReturn(new HashSet<>());

        membershipBean.askToJoinProject(projectId, securityContext);

        verify(projectMemberDao, times(1)).persist(any(ProjectMembershipEntity.class));
        verify(notificationBean, times(1)).createProjectJoinRequestNotificationsForProjectAdmins(any(ProjectMembershipEntity.class));
    }




    @Test
    void testAskToJoinProject_UserNotFound() {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            membershipBean.askToJoinProject(projectId, securityContext);
        });

        assertEquals("User not found", thrown.getMessage());
    }

    @Test
    void testAskToJoinProject_ProjectNotFound() {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        UserEntity userEntity = mock(UserEntity.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(userEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            membershipBean.askToJoinProject(projectId, securityContext);
        });

        assertEquals("Project not found", thrown.getMessage());
    }

    @Test
    void testAskToJoinProject_ProjectNotEditable() {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        UserEntity userEntity = mock(UserEntity.class);
        ProjectEntity projectEntity = mock(ProjectEntity.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(userEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(projectEntity.getState()).thenReturn(ProjectStateEnum.FINISHED);

        ElementAssociationException thrown = assertThrows(ElementAssociationException.class, () -> {
            membershipBean.askToJoinProject(projectId, securityContext);
        });

        assertEquals("Project is not editable anymore", thrown.getMessage());
    }

    @Test
    void testAskToJoinProject_UserAlreadyMember() {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        UserEntity userEntity = mock(UserEntity.class);
        ProjectEntity projectEntity = mock(ProjectEntity.class);
        ProjectMembershipEntity membershipEntity = mock(ProjectMembershipEntity.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(userEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(projectEntity.getState()).thenReturn(ProjectStateEnum.PLANNING);
        when(configurationBean.getConfigValueByKey("maxProjectMembers")).thenReturn(10); // Ensure member limit is not reached
        when(projectEntity.getMembers()).thenReturn(new HashSet<>(Collections.singletonList(membershipEntity)));
        when(membershipEntity.getUser()).thenReturn(userEntity);

        // Execute and verify
        DuplicatedAttributeException thrown = assertThrows(DuplicatedAttributeException.class, () -> {
            membershipBean.askToJoinProject(projectId, securityContext);
        });

        assertEquals("User is already member of the project", thrown.getMessage());
    }



    @Test
    void testAskToJoinProject_ProjectMembersLimitReached() {
        long projectId = 1L;
        long userId = 2L;
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        UserEntity userEntity = mock(UserEntity.class);
        ProjectEntity projectEntity = mock(ProjectEntity.class);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(authUserDto.getUserId()).thenReturn(userId);
        when(userDao.findUserById(userId)).thenReturn(userEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(projectEntity.getState()).thenReturn(ProjectStateEnum.PLANNING);
        when(configurationBean.getConfigValueByKey("maxProjectMembers")).thenReturn(1);
        when(projectEntity.getMembers()).thenReturn(new HashSet<>(Collections.singletonList(new ProjectMembershipEntity())));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            membershipBean.askToJoinProject(projectId, securityContext);
        });

        assertEquals("Project member's limit is reached", thrown.getMessage());
    }
}
