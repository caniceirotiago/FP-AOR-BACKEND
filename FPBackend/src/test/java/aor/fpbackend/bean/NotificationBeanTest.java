package aor.fpbackend.bean;

import aor.fpbackend.dao.NotificationDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.Notification.NotificationGetDto;
import aor.fpbackend.dto.User.UserBasicInfoDto;
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

        RoleEntity role = new RoleEntity(UserRoleEnum.ADMIN);
        role.setId(1L); // Initialize the role id

        UserEntity recipient = new UserEntity();
        recipient.setId(1L);
        recipient.setUsername("recipientUser");
        recipient.setPhoto("http://photo.url/recipient");
        recipient.setRole(role);

        UserEntity sender = new UserEntity();
        sender.setId(2L);
        sender.setUsername("senderUser");
        sender.setPhoto("http://photo.url/sender");
        sender.setRole(role);

        messageEntity.setRecipient(recipient);
        messageEntity.setSender(sender);

        when(userBean.convertUserEntitytoUserBasicInfoDto(recipient)).thenReturn(new UserBasicInfoDto(recipient.getId(), recipient.getUsername(), recipient.getPhoto(), recipient.getRole().getId()));
        when(userBean.convertUserEntitytoUserBasicInfoDto(sender)).thenReturn(new UserBasicInfoDto(sender.getId(), sender.getUsername(), sender.getPhoto(), sender.getRole().getId()));

        notificationBean.createIndividualMessageNotification(messageEntity);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateProjectJoinRequestNotificationsForProjectAdmins() throws UnknownHostException {
        RoleEntity role = new RoleEntity(UserRoleEnum.ADMIN);
        role.setId(1L); // Initialize the role id

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPhoto("http://photo.url/testUser");
        userEntity.setRole(role);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);

        ProjectMembershipEntity adminMembership = new ProjectMembershipEntity();
        adminMembership.setUser(userEntity);
        adminMembership.setRole(ProjectRoleEnum.PROJECT_MANAGER);

        projectEntity.setMembers(Set.of(adminMembership));

        when(userBean.convertUserEntitytoUserBasicInfoDto(any(UserEntity.class))).thenReturn(new UserBasicInfoDto(userEntity.getId(), userEntity.getUsername(), userEntity.getPhoto(), userEntity.getRole().getId()));

        notificationBean.createProjectJoinRequestNotificationsForProjectAdmins(projectMembershipEntity);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateNotificationForProjectJoinRequestApprovedOrRejected() throws UnknownHostException {
        RoleEntity role = new RoleEntity(UserRoleEnum.ADMIN);
        role.setId(1L); // Initialize the role id

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPhoto("http://photo.url/testUser");
        userEntity.setRole(role);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);

        when(userBean.convertUserEntitytoUserBasicInfoDto(any(UserEntity.class))).thenReturn(new UserBasicInfoDto(userEntity.getId(), userEntity.getUsername(), userEntity.getPhoto(), userEntity.getRole().getId()));

        notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(projectMembershipEntity, true);

        verify(notificationDao, times(1)).persist(any(NotificationEntity.class));
    }

    @Test
    void testCreateNotificationForUserRemovedFromProject() throws UnknownHostException {
        RoleEntity role = new RoleEntity(UserRoleEnum.ADMIN);
        role.setId(1L); // Initialize the role id

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPhoto("http://photo.url/testUser");
        userEntity.setRole(role);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("testProject");

        ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
        projectMembershipEntity.setUser(userEntity);
        projectMembershipEntity.setProject(projectEntity);
        projectMembershipEntity.setAccepted(true);

        when(userBean.convertUserEntitytoUserBasicInfoDto(any(UserEntity.class))).thenReturn(new UserBasicInfoDto(userEntity.getId(), userEntity.getUsername(), userEntity.getPhoto(), userEntity.getRole().getId()));

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
