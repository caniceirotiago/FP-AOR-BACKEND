package aor.fpbackend.bean;

import aor.fpbackend.dao.GroupMessageDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.GroupMessage.GroupMessageGetDto;
import aor.fpbackend.dto.GroupMessage.GroupMessageSendDto;
import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupMessageBeanTest {

    @InjectMocks
    private GroupMessageBean groupMessageBean;

    @Mock
    private UserBean userBean;

    @Mock
    private UserDao userDao;

    @Mock
    private GroupMessageDao groupMessageDao;

    @Mock
    private ProjectMembershipDao projectMemberDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendGroupMessage_Success() throws UserNotFoundException, EntityNotFoundException {
        long senderId = 1L;
        long groupId = 1L;
        String content = "Test Message";
        GroupMessageSendDto dto = new GroupMessageSendDto(content, senderId, groupId);

        UserEntity sender = new UserEntity();
        sender.setId(senderId);
        ProjectEntity project = new ProjectEntity();
        project.setId(groupId);

        when(userDao.findUserById(senderId)).thenReturn(sender);
        when(projectDao.findProjectById(groupId)).thenReturn(project);

        GroupMessageEntity message = groupMessageBean.sendGroupMessage(dto);

        assertNotNull(message);
        assertEquals(content, message.getContent());
        verify(groupMessageDao, times(1)).persist(any(GroupMessageEntity.class));
    }

    @Test
    void testSendGroupMessage_UserNotFound() {
        long senderId = 1L;
        long groupId = 1L;
        String content = "Test Message";
        GroupMessageSendDto dto = new GroupMessageSendDto(content, senderId, groupId);

        when(userDao.findUserById(senderId)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> groupMessageBean.sendGroupMessage(dto));
    }

    @Test
    void testSendGroupMessage_ProjectNotFound() {
        long senderId = 1L;
        long groupId = 1L;
        String content = "Test Message";
        GroupMessageSendDto dto = new GroupMessageSendDto(content, senderId, groupId);

        UserEntity sender = new UserEntity();
        sender.setId(senderId);

        when(userDao.findUserById(senderId)).thenReturn(sender);
        when(projectDao.findProjectById(groupId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> groupMessageBean.sendGroupMessage(dto));
    }

    @Test
    void testGetGroupMessagesByProjectId_Success() throws UserNotFoundException {
        long projectId = 1L;
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        GroupMessageEntity message = new GroupMessageEntity();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        message.setGroup(project);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(user);
        when(groupMessageDao.getGroupMessagesByProjectId(projectId)).thenReturn(Collections.singletonList(message));

        List<GroupMessageGetDto> result = groupMessageBean.getGroupMessagesByProjectId(projectId, securityContext);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    void testGetGroupMessagesByProjectId_UserNotFound() {
        long projectId = 1L;
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> groupMessageBean.getGroupMessagesByProjectId(projectId, securityContext));
    }

    @Test
    void testGetGroupMessagesByMessageIds_Success() {
        List<Long> messageIds = Collections.singletonList(1L);
        GroupMessageEntity message = new GroupMessageEntity();
        ProjectEntity project = new ProjectEntity();
        project.setId(1L); // Defina o ID do projeto
        message.setGroup(project); // Associe o projeto ao grupo

        when(groupMessageDao.getMessagesByIds(messageIds)).thenReturn(Collections.singletonList(message));

        List<GroupMessageGetDto> result = groupMessageBean.getGroupMessagesByMessageIds(messageIds);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    void testMarkMessageAsReadByUser_Success() {
        Long messageId = 1L;
        Long userId = 1L;
        GroupMessageEntity message = new GroupMessageEntity();
        UserEntity user = new UserEntity();

        when(groupMessageDao.findGroupMessageById(messageId)).thenReturn(message);
        when(userDao.findUserById(userId)).thenReturn(user);

        groupMessageBean.markMessageAsReadByUser(messageId, userId);

        assertTrue(message.getReadByUsers().contains(user));
        verify(groupMessageDao, times(1)).findGroupMessageById(messageId);
    }

    @Test
    void testVerifyMessagesAsReadForGroup_Success() {
        List<Long> messageIds = Collections.singletonList(1L);
        Long userId = 1L;
        GroupMessageEntity message = new GroupMessageEntity();
        UserEntity user = new UserEntity();
        user.setId(userId);
        message.setId(1L);
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        message.setGroup(project);
        message.setReadByUsers(new HashSet<>(Set.of(user))); // Use um conjunto mutável

        when(groupMessageDao.findGroupMessageById(anyLong())).thenReturn(message);
        when(userDao.findUserById(userId)).thenReturn(user);

        boolean result = groupMessageBean.verifyMessagesAsReadForGroup(messageIds, userId);

        assertTrue(result);
    }


}
