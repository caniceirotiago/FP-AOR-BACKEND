package aor.fpbackend.bean;

import aor.fpbackend.dao.IndividualMessageDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.dto.UserBasicInfoDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.Instant;
import java.util.List;

@Stateless
public class IndividualMessageBean {
    @EJB
    UserDao userDao;
    @EJB
    IndividualMessageDao individualMessageDao;
    @EJB
    UserBean userBean;


    public void sendIndividualMessage(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        if (individualMessageSendDto == null) {
            throw new IllegalArgumentException("Invalid Dto");
        }
        IndividualMessageEntity individualMessageEntity = convertToEntity(individualMessageSendDto);
        individualMessageEntity.setSentTime(Instant.now());
        individualMessageEntity.setViewed(false);
        individualMessageDao.persist(individualMessageEntity);
    }
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
    public List<IndividualMessageGetDto> getReceivedMessages(String userId) throws UserNotFoundException {
        boolean userExists = userDao.confirmUserIdExists(userId);
        if (!userExists) {
            throw new UserNotFoundException("Invalid user id");
        }
        List<IndividualMessageEntity> receivedMessages = individualMessageDao.getReceivedMessages(userId);
        return convertToDtos(receivedMessages);
    }

    public List<IndividualMessageGetDto> getSentMessages(String userId) throws UserNotFoundException {
        boolean userExists = userDao.confirmUserIdExists(userId);
        if (!userExists) {
            throw new UserNotFoundException("Invalid user id");
        }
        List<IndividualMessageEntity> sentMessages = individualMessageDao.getSentMessages(userId);
        return convertToDtos(sentMessages);
    }

    private IndividualMessageGetDto convertToDto(IndividualMessageEntity individualMessageEntity) {
        IndividualMessageGetDto individualMessageGetDto = new IndividualMessageGetDto();
        individualMessageGetDto.setContent(individualMessageEntity.getContent());
        individualMessageGetDto.setRecipient(userBean.convertUserEntetyToUserBasicInfoDto(individualMessageEntity.getRecipient()));
        individualMessageGetDto.setSender(userBean.convertUserEntetyToUserBasicInfoDto(individualMessageEntity.getSender()));
        individualMessageGetDto.setSentAt(individualMessageEntity.getSentTime());
        individualMessageGetDto.setSubject(individualMessageEntity.getSubject());
        individualMessageGetDto.setViewed(individualMessageEntity.isViewed());
        return individualMessageGetDto;
    }
    private IndividualMessageEntity convertToEntity(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        UserEntity sender = null;
        UserEntity recipient = null;
        try {
            sender = userDao.find(individualMessageSendDto.getSenderId());
            recipient = userDao.find(individualMessageSendDto.getRecipientId());
        } catch (Exception e) {
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
