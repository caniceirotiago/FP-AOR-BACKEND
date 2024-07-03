package aor.fpbackend.bean;

import aor.fpbackend.dao.IndividualMessageDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetPaginatedDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageSendDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndividualMessageBeanTest {

    @InjectMocks
    private IndividualMessageBean individualMessageBean;

    @Mock
    private UserDao userDao;

    @Mock
    private IndividualMessageDao individualMessageDao;

    @Mock
    private UserBean userBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendIndividualMessage_Success() throws UserNotFoundException {
        IndividualMessageSendDto sendDto = new IndividualMessageSendDto("senderId", 1L, 2L, "content");
        UserEntity sender = new UserEntity();
        UserEntity recipient = new UserEntity();

        when(userDao.find("senderId")).thenReturn(sender);
        when(userDao.find("recipientId")).thenReturn(recipient);

        IndividualMessageEntity result = individualMessageBean.sendIndividualMessage(sendDto);

        assertNotNull(result);
        assertEquals("content", result.getContent());
        assertEquals("subject", result.getSubject());
        assertEquals(sender, result.getSender());
        assertEquals(recipient, result.getRecipient());
        assertFalse(result.isViewed());
        assertNotNull(result.getSentTime());

        verify(individualMessageDao, times(1)).persist(any(IndividualMessageEntity.class));
    }

    @Test
    void testSendIndividualMessage_UserNotFound() {
        IndividualMessageSendDto sendDto = new IndividualMessageSendDto("senderId", 21L, 2L, "content");

        when(userDao.find("senderId")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> individualMessageBean.sendIndividualMessage(sendDto));
    }

    @Test
    void testGetIndividualMessages_Success() throws UserNotFoundException {
        String senderId = "senderId";
        String recipientId = "recipientId";
        IndividualMessageEntity message = new IndividualMessageEntity();

        when(userDao.confirmUserIdExists(senderId)).thenReturn(true);
        when(userDao.confirmUserIdExists(recipientId)).thenReturn(true);
        when(individualMessageDao.getIndividualMessages(senderId, recipientId)).thenReturn(Collections.singletonList(message));

        List<IndividualMessageGetDto> result = individualMessageBean.getIndividualMessages(senderId, recipientId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(individualMessageDao, times(1)).getIndividualMessages(senderId, recipientId);
    }

    @Test
    void testGetIndividualMessages_UserNotFound() {
        String senderId = "senderId";
        String recipientId = "recipientId";

        when(userDao.confirmUserIdExists(senderId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> individualMessageBean.getIndividualMessages(senderId, recipientId));
    }

    @Test
    void testGetFilteredMessages_Sent_Success() throws UserNotFoundException {
        String userId = "userId";
        int page = 0;
        int pageSize = 10;
        UriInfo uriInfo = mock(UriInfo.class);
        IndividualMessageEntity message = new IndividualMessageEntity();

        when(userDao.confirmUserIdExists(userId)).thenReturn(true);
        when(individualMessageDao.findSentMessages(userId, page, pageSize, uriInfo)).thenReturn(Collections.singletonList(message));
        when(individualMessageDao.countSentMessages(userId, uriInfo)).thenReturn(1L);

        IndividualMessageGetPaginatedDto result = individualMessageBean.getFilteredMessages(userId, "sent", page, pageSize, uriInfo);

        assertNotNull(result);
        assertEquals(1, result.getMessages().size());
        assertEquals(1L, result.getTotalMessages());

        verify(individualMessageDao, times(1)).findSentMessages(userId, page, pageSize, uriInfo);
        verify(individualMessageDao, times(1)).countSentMessages(userId, uriInfo);
    }

    @Test
    void testGetFilteredMessages_Inbox_Success() throws UserNotFoundException {
        String userId = "userId";
        int page = 0;
        int pageSize = 10;
        UriInfo uriInfo = mock(UriInfo.class);
        IndividualMessageEntity message = new IndividualMessageEntity();

        when(userDao.confirmUserIdExists(userId)).thenReturn(true);
        when(individualMessageDao.findReceivedMessages(userId, page, pageSize, uriInfo)).thenReturn(Collections.singletonList(message));
        when(individualMessageDao.countReceivedMessages(userId, uriInfo)).thenReturn(1L);

        IndividualMessageGetPaginatedDto result = individualMessageBean.getFilteredMessages(userId, "inbox", page, pageSize, uriInfo);

        assertNotNull(result);
        assertEquals(1, result.getMessages().size());
        assertEquals(1L, result.getTotalMessages());

        verify(individualMessageDao, times(1)).findReceivedMessages(userId, page, pageSize, uriInfo);
        verify(individualMessageDao, times(1)).countReceivedMessages(userId, uriInfo);
    }

    @Test
    void testGetFilteredMessages_InvalidType() {
        String userId = "userId";
        int page = 0;
        int pageSize = 10;
        UriInfo uriInfo = mock(UriInfo.class);

        when(userDao.confirmUserIdExists(userId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> individualMessageBean.getFilteredMessages(userId, "invalid", page, pageSize, uriInfo));
    }

    @Test
    void testMarkMessagesAsRead_Success() {
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);

        when(individualMessageDao.markMessagesAsRead(messageIds)).thenReturn(true);

        boolean result = individualMessageBean.markMessagesAsRead(messageIds);

        assertTrue(result);

        verify(individualMessageDao, times(1)).markMessagesAsRead(messageIds);
    }

    @Test
    void testGetMessagesByIds_Success() {
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);
        IndividualMessageEntity message = new IndividualMessageEntity();

        when(individualMessageDao.getMessagesByIds(messageIds)).thenReturn(Collections.singletonList(message));

        List<IndividualMessageGetDto> result = individualMessageBean.getMessagesByIds(messageIds);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(individualMessageDao, times(1)).getMessagesByIds(messageIds);
    }
}
