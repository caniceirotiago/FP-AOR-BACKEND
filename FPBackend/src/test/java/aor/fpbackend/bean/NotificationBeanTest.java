package aor.fpbackend.bean;

import aor.fpbackend.dao.NotificationDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.Notification.NotificationGetDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.NotificationTypeENUM;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.websocket.GlobalWebSocket;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationBeanTest {

    @InjectMocks
    private NotificationBean notificationBean;

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private UserBean userBean;

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateIndividualMessageNotification() throws UnknownHostException {
        IndividualMessageEntity messageEntity = new IndividualMessageEntity();
        UserEntity recipient = new UserEntity();
        recipient.setUsername("recipientUser");
        UserEntity sender = new UserEntity();
        sender.setUsername("senderUser");
        messageEntity.setRecipient(recipient);
        messageEntity.setSender(sender);

        notificationBean.createIndividualMessageNotification(messageEntity);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateProjectJoinRequestNotificationsForProjectAdmins() throws UnknownHostException {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);

        ProjectMembershipEntity adminMembership = new ProjectMembershipEntity();
        adminMembership.setUser(userEntity);
        adminMembership.setRole(ProjectRoleEnum.PROJECT_MANAGER);

        projectEntity.setMembers(Set.of(adminMembership));

        notificationBean.createProjectJoinRequestNotificationsForProjectAdmins(projectMembershipEntity);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateNotificationForProjectJoinRequestApprovedOrRejected() throws UnknownHostException {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);

        notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(projectMembershipEntity, true);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateNotificationForUserRemovedFromProject() throws UnknownHostException {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);
        projectMembershipEntity.setAccepted(true);

        notificationBean.createNotificationForUserRemovedFromProject(projectMembershipEntity);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testGetUnreadNotifications() {
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(notificationDao.getUnreadbByUserNotifications(1L)).thenReturn(new ArrayList<>());

        List<NotificationGetDto> unreadNotifications = notificationBean.getUnreadNotifications(securityContext);

        assertNotNull(unreadNotifications);
        verify(notificationDao, times(1)).getUnreadbByUserNotifications(1L);
    }

    @Test
    void testMarkNotificationsAsRead() throws EntityNotFoundException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(1L);

        when(notificationDao.findNotificationById(1L)).thenReturn(notificationEntity);

        notificationBean.markNotificationsAsRead(1L);

        verify(notificationDao, times(1)).merge(notificationEntity);
        assertTrue(notificationEntity.isRead());
    }

    @Test
    void testMarkNotificationsAsRead_NotFound() {
        when(notificationDao.findNotificationById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> notificationBean.markNotificationsAsRead(1L));
    }
}
