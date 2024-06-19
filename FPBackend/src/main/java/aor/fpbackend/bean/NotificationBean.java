package aor.fpbackend.bean;

import aor.fpbackend.dao.NotificationDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.NotificationGetDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.NotificationEntity;
import aor.fpbackend.enums.NotificationTypeENUM;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class NotificationBean implements Serializable {
    @EJB
    NotificationDao notificationDao;
    @EJB
    UserBean userBean;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(NotificationBean.class);

    public void createIndividualMessageNotification(IndividualMessageEntity messageEntity) {

        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.INDIVIDUAL_MESSAGE);
        notificationEntity.setUser(messageEntity.getRecipient());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setIndividualMessage(messageEntity);
        notificationEntity.setContent("You have a new message from " + messageEntity.getSender().getUsername());
        LOGGER.info("Creating notification for individual message");
        notificationDao.persist(notificationEntity);
    }

    public List<NotificationGetDto> getUnreadNotifications(SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        List<NotificationEntity> notificationsEntety =  notificationDao.getUnreadbByUserNotifications(authUserDto.getUserId());
        return convertEntetiesToDtos(notificationsEntety);
    }

    private List<NotificationGetDto> convertEntetiesToDtos(List<NotificationEntity> notificationsEntety) {
        List<NotificationGetDto> notificationGetDtos = new ArrayList<>();
        for (NotificationEntity notificationEntity : notificationsEntety) {
           NotificationGetDto newDto = convertEntetyToDto(notificationEntity);
            notificationGetDtos.add(newDto);
        }
        System.out.println("Notifications: " + notificationGetDtos.size());
        System.out.println(notificationGetDtos);
        return notificationGetDtos;
    }

    private NotificationGetDto convertEntetyToDto(NotificationEntity notificationEntity) {
        NotificationGetDto notificationGetDto = new NotificationGetDto();
        notificationGetDto.setId(notificationEntity.getId());
        notificationGetDto.setType(notificationEntity.getType());
        notificationGetDto.setContent(notificationEntity.getContent());
        notificationGetDto.setDateTime(notificationEntity.getDateTime());
        notificationGetDto.setRead(notificationEntity.isRead());
        notificationGetDto.setUser(userBean.convertUserEntitytoUserBasicInfoDto(notificationEntity.getUser()));
        if(notificationEntity.getIndividualMessage() != null)notificationGetDto.setIndividualMessage(new IndividualMessageGetDto(
                notificationEntity.getIndividualMessage().getId(),
                notificationEntity.getIndividualMessage().getContent(),
                userBean.convertUserEntetyToUserBasicInfoDto(notificationEntity.getIndividualMessage().getSender()),
                userBean.convertUserEntetyToUserBasicInfoDto(notificationEntity.getIndividualMessage().getRecipient()),
                notificationEntity.getIndividualMessage().getSubject(),
                notificationEntity.getIndividualMessage().getSentTime(),
                notificationEntity.getIndividualMessage().isViewed()
        ));
        return notificationGetDto;
    }

}
