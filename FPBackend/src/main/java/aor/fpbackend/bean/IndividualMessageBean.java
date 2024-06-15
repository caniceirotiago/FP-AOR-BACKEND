package aor.fpbackend.bean;

import aor.fpbackend.dao.IndividualMessageDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
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


    public void sendIndividualMessage(IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        if (individualMessageSendDto == null) {
            throw new IllegalArgumentException("Invalid Dto");
        }
        IndividualMessageEntity individualMessageEntity = convertToEntity(individualMessageSendDto);
        individualMessageEntity.setSentTime(Instant.now());
        individualMessageEntity.setViewed(false);
        individualMessageDao.persist(individualMessageEntity);
    }
    public List<IndividualMessageGetDto> getIndividualMessages(String senderId, String recipientId){
        return null;
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
