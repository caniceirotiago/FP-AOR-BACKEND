package aor.fpbackend.bean;

import aor.fpbackend.dao.IndividualMessageDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetPaginatedDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageSendDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.InputValidationException;
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
     * This method converts the provided DTO into an entity, sets the current time as the sent time,
     * marks the message as not viewed, and persists the entity to the database.
     *
     * @param individualMessageSendDto the DTO containing message details
     * @return the created IndividualMessageEntity
     * @throws UserNotFoundException if the sender or recipient is not found
     */
    public IndividualMessageEntity sendIndividualMessage(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        // Convert the DTO to an entity to be used for persistence
        IndividualMessageEntity individualMessageEntity = convertToEntity(individualMessageSendDto);
        // Set the current time as the message's sent time
        individualMessageEntity.setSentTime(Instant.now());
        // Mark the message as not viewed initially
        individualMessageEntity.setViewed(false);
        // Persist the entity to the database
        individualMessageDao.persist(individualMessageEntity);
        return individualMessageEntity;
    }

    /**
     * Retrieves individual messages between a sender and a recipient.
     *
     * @param senderId    the ID of the sender
     * @param recipientId the ID of the recipient
     * @return a list of IndividualMessageGetDto
     * @throws UserNotFoundException if the sender or recipient is not found
     */
    public List<IndividualMessageGetDto> getIndividualMessages(String senderId, String recipientId) throws UserNotFoundException {
        // Check if the sender exists
        boolean senderExists = userDao.confirmUserIdExists(senderId);
        // Check if the recipient exists
        boolean recipientExists = userDao.confirmUserIdExists(recipientId);
        if (senderExists == false || recipientExists == false) {
            throw new UserNotFoundException("Invalid sender or recipient id");
        }
        // Retrieve the messages between the sender and recipient from the database
        List<IndividualMessageEntity> individualMessageEntities = individualMessageDao.getIndividualMessages(senderId, recipientId);
        // Convert the retrieved message entities to DTOs
        List<IndividualMessageGetDto> individualMessageGetDtos = convertToDtos(individualMessageEntities);
        return individualMessageGetDtos;
    }

    /**
     * Converts a list of IndividualMessageEntity objects to a list of IndividualMessageGetDto objects.
     * <p>
     * This method uses Java Streams to map each IndividualMessageEntity to an IndividualMessageGetDto
     * by invoking the `convertToDto` method on each entity.
     *
     * @param individualMessageEntities the list of IndividualMessageEntity objects to be converted.
     * @return a list of IndividualMessageGetDto objects corresponding to the input entities.
     */
    private List<IndividualMessageGetDto> convertToDtos(List<IndividualMessageEntity> individualMessageEntities) {
        // Use Java Streams to map each IndividualMessageEntity to IndividualMessageGetDto and collect the results into a list
        return individualMessageEntities.stream().map(this::convertToDto).toList();
    }

    /**
     * Retrieves paginated individual messages filtered by type.
     * <p>
     * This method checks if the user exists, and based on the type of messages
     * ("sent" or "inbox"), retrieves the appropriate messages from the database.
     * It then converts the message entities to DTOs, calculates the total number
     * of messages, and returns a paginated DTO containing the messages and total count.
     *
     * @param userId   the ID of the user
     * @param type     the type of messages ("sent" or "inbox")
     * @param page     the page number for pagination
     * @param pageSize the number of messages per page
     * @param uriInfo  the URI info for additional filtering
     * @return a paginated DTO of messages
     * @throws UserNotFoundException if the user is not found
     *                               * @throws IllegalArgumentException if the message type is invalid
     */
    public IndividualMessageGetPaginatedDto getFilteredMessages(String userId, String type, int page, int pageSize, UriInfo uriInfo) throws UserNotFoundException, InputValidationException {
        // Check if the user exists
        boolean userExists = userDao.confirmUserIdExists(userId);
        if (!userExists) {
            throw new UserNotFoundException("Invalid user id");
        }
        if (!"sent".equalsIgnoreCase(type) && !"inbox".equalsIgnoreCase(type)) {
            throw new InputValidationException("Invalid message type: " + type);
        }
        List<IndividualMessageEntity> messageEntities;
        long totalMessages;
        // Retrieve sent or inbox messages based on the provided type
        if ("sent".equalsIgnoreCase(type)) {
            messageEntities = individualMessageDao.findSentMessages(userId, page, pageSize, uriInfo);
            totalMessages = individualMessageDao.countSentMessages(userId, uriInfo);
        } else if ("inbox".equalsIgnoreCase(type)) {
            messageEntities = individualMessageDao.findReceivedMessages(userId, page, pageSize, uriInfo);
            totalMessages = individualMessageDao.countReceivedMessages(userId, uriInfo);
        } else {
            // Throw an exception if the message type is invalid
            throw new IllegalArgumentException("Invalid message type: " + type);
        }
        // Convert the retrieved message entities to DTOs
        List<IndividualMessageGetDto> messageDtos = messageEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        // Create and populate the paginated DTO with messages and total count
        IndividualMessageGetPaginatedDto paginatedMessagesDto = new IndividualMessageGetPaginatedDto();
        paginatedMessagesDto.setMessages(messageDtos);
        paginatedMessagesDto.setTotalMessages(totalMessages);
        // Return the paginated DTO
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

    /**
     * Converts an IndividualMessageEntity object to an IndividualMessageGetDto object.
     *
     * This method maps the properties of the provided IndividualMessageEntity to a new
     * IndividualMessageGetDto object. It includes the content, sender, recipient, sent time,
     * subject, viewed status, and ID of the message.
     *
     * @param individualMessageEntity the IndividualMessageEntity object to be converted
     * @return an IndividualMessageGetDto object with the mapped properties
     */
    public IndividualMessageGetDto convertToDto(IndividualMessageEntity individualMessageEntity) {
        // Create a new DTO object
        IndividualMessageGetDto individualMessageGetDto = new IndividualMessageGetDto();
        // Map the contents of the entity to the DTO
        individualMessageGetDto.setContent(individualMessageEntity.getContent());
        individualMessageGetDto.setRecipient(userBean.convertUserEntitytoUserBasicInfoDto(individualMessageEntity.getRecipient()));
        individualMessageGetDto.setSender(userBean.convertUserEntitytoUserBasicInfoDto(individualMessageEntity.getSender()));
        individualMessageGetDto.setSentAt(individualMessageEntity.getSentTime());
        individualMessageGetDto.setSubject(individualMessageEntity.getSubject());
        individualMessageGetDto.setViewed(individualMessageEntity.isViewed());
        individualMessageGetDto.setId(individualMessageEntity.getId());
        // Return the fully populated DTO
        return individualMessageGetDto;
    }

    /**
     * Converts an IndividualMessageSendDto object to an IndividualMessageEntity object.
     *
     * This method retrieves the sender and recipient UserEntity objects based on the IDs
     * provided in the DTO. If either user is not found, a UserNotFoundException is thrown.
     * It then maps the properties from the DTO to a new IndividualMessageEntity object.
     *
     * @param individualMessageSendDto the DTO containing message details to be converted
     * @return an IndividualMessageEntity object with the mapped properties
     * @throws UserNotFoundException if the sender or recipient is not found
     */
    private IndividualMessageEntity convertToEntity(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        // Retrieve the sender and recipient UserEntity based on their ID from the DTO
        UserEntity sender = userDao.find(individualMessageSendDto.getSenderId());
        UserEntity recipient = userDao.find(individualMessageSendDto.getRecipientId());
        if (sender == null || recipient == null) {
            throw new UserNotFoundException("Invalid sender or recipient id");
        }
        // Create a new IndividualMessageEntity object
        IndividualMessageEntity individualMessageEntity = new IndividualMessageEntity();
        // Map the contents from the DTO to the entity
        individualMessageEntity.setContent(individualMessageSendDto.getContent());
        individualMessageEntity.setSender(sender);
        individualMessageEntity.setRecipient(recipient);
        individualMessageEntity.setSubject(individualMessageSendDto.getSubject());
        // Return the fully populated entity
        return individualMessageEntity;
    }
}
