package aor.fpbackend.bean;

import aor.fpbackend.dao.IndividualMessageDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetPaginatedDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageSendDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class IndividualMessageBean {

    private static final long serialVersionUID = 1L;

    @EJB
    UserDao userDao;
    @EJB
    IndividualMessageDao individualMessageDao;
    @EJB
    UserBean userBean;


    /**
     * Sends an individual message.
     *
     * @param individualMessageSendDto the DTO containing message details
     * @return the created IndividualMessageEntity
     * @throws UserNotFoundException if the sender or recipient is not found
     */
    public IndividualMessageEntity sendIndividualMessage(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {

        IndividualMessageEntity individualMessageEntity = convertToEntity(individualMessageSendDto);
        individualMessageEntity.setSentTime(Instant.now());
        individualMessageEntity.setViewed(false);
        individualMessageDao.persist(individualMessageEntity);
        return individualMessageEntity;
    }
    /**
     * Retrieves individual messages between a sender and a recipient.
     *
     * @param senderId the ID of the sender
     * @param recipientId the ID of the recipient
     * @return a list of IndividualMessageGetDto
     * @throws UserNotFoundException if the sender or recipient is not found
     */
    public List<IndividualMessageGetDto> getIndividualMessages(String senderId, String recipientId) throws UserNotFoundException {
        boolean senderExists = userDao.confirmUserIdExists(senderId);
        boolean recipientExists = userDao.confirmUserIdExists(recipientId);
        if (senderExists == false || recipientExists == false) {
            throw new UserNotFoundException("Invalid sender or recipient id");
        }
        List<IndividualMessageEntity> individualMessageEntities = individualMessageDao.getIndividualMessages(senderId, recipientId);
        List<IndividualMessageGetDto> individualMessageGetDtos = convertToDtos(individualMessageEntities);
        return individualMessageGetDtos;
    }

    private List<IndividualMessageGetDto> convertToDtos(List<IndividualMessageEntity> individualMessageEntities) {
        return individualMessageEntities.stream().map(this::convertToDto).toList();
    }

    /**
     * Retrieves paginated individual messages filtered by type.
     *
     * @param userId the ID of the user
     * @param type the type of messages ("sent" or "inbox")
     * @param page the page number
     * @param pageSize the page size
     * @param uriInfo the URI info for additional filtering
     * @return a paginated DTO of messages
     * @throws UserNotFoundException if the user is not found
     */
    public IndividualMessageGetPaginatedDto getFilteredMessages(String userId, String type, int page, int pageSize, UriInfo uriInfo) throws UserNotFoundException {
        boolean userExists = userDao.confirmUserIdExists(userId);
        if (!userExists) {
            throw new UserNotFoundException("Invalid user id");
        }

        List<IndividualMessageEntity> messageEntities;
        long totalMessages;

        if ("sent".equalsIgnoreCase(type)) {
            messageEntities = individualMessageDao.findSentMessages(userId, page, pageSize, uriInfo);
            totalMessages = individualMessageDao.countSentMessages(userId, uriInfo);
        } else if ("inbox".equalsIgnoreCase(type)) {
            messageEntities = individualMessageDao.findReceivedMessages(userId, page, pageSize, uriInfo);
            totalMessages = individualMessageDao.countReceivedMessages(userId, uriInfo);
        } else {
            throw new IllegalArgumentException("Invalid message type: " + type);
        }

        List<IndividualMessageGetDto> messageDtos = messageEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        IndividualMessageGetPaginatedDto paginatedMessagesDto = new IndividualMessageGetPaginatedDto();
        paginatedMessagesDto.setMessages(messageDtos);
        paginatedMessagesDto.setTotalMessages(totalMessages);

        return paginatedMessagesDto;
    }

    /**
     * Marks messages as read.
     *
     * @param messageIds the list of message IDs to be marked as read
     * @return true if successful, false otherwise
     */
    public boolean markMessagesAsRead(List<Long> messageIds) {
        return individualMessageDao.markMessagesAsRead(messageIds);
    }

    /**
     * Retrieves messages by their IDs.
     *
     * @param messageIds the list of message IDs
     * @return a list of IndividualMessageGetDto
     */
    public List<IndividualMessageGetDto> getMessagesByIds(List<Long> messageIds) {
        List<IndividualMessageEntity> messages = individualMessageDao.getMessagesByIds(messageIds);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }



    public IndividualMessageGetDto convertToDto(IndividualMessageEntity individualMessageEntity) {
        IndividualMessageGetDto individualMessageGetDto = new IndividualMessageGetDto();
        individualMessageGetDto.setContent(individualMessageEntity.getContent());
        individualMessageGetDto.setRecipient(userBean.convertUserEntitytoUserBasicInfoDto(individualMessageEntity.getRecipient()));
        individualMessageGetDto.setSender(userBean.convertUserEntitytoUserBasicInfoDto(individualMessageEntity.getSender()));
        individualMessageGetDto.setSentAt(individualMessageEntity.getSentTime());
        individualMessageGetDto.setSubject(individualMessageEntity.getSubject());
        individualMessageGetDto.setViewed(individualMessageEntity.isViewed());
        individualMessageGetDto.setId(individualMessageEntity.getId());
        return individualMessageGetDto;
    }

    private IndividualMessageEntity convertToEntity(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        UserEntity sender = userDao.find(individualMessageSendDto.getSenderId());
        UserEntity recipient = userDao.find(individualMessageSendDto.getRecipientId());
        if (sender == null || recipient == null) {
            throw new UserNotFoundException("Invalid sender or recipient id");
        }
        IndividualMessageEntity individualMessageEntity = new IndividualMessageEntity();
        individualMessageEntity.setContent(individualMessageSendDto.getContent());
        individualMessageEntity.setSender(sender);
        individualMessageEntity.setRecipient(recipient);
        individualMessageEntity.setSubject(individualMessageSendDto.getSubject());
        return individualMessageEntity;
    }
}
