package aor.fpbackend.bean;

import aor.fpbackend.dao.NotificationDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.NotificationGetDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.NotificationTypeENUM;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.websocket.GlobalWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Stateless
public class NotificationBean implements Serializable {
    @EJB
    NotificationDao notificationDao;
    @EJB
    UserBean userBean;
    @EJB
    UserDao userDao;
    @EJB
    RoleDao roleDao;
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
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createProjectJoinRequestNotificationsForProjectAdmins(ProjectMembershipEntity projectMembershipEn) {
        Set<ProjectMembershipEntity> projectMembershipEntitiesSet = projectMembershipEn.getProject().getMembers();
        List<ProjectMembershipEntity> projectMembershipEntities = new ArrayList<>(projectMembershipEntitiesSet);

        for (ProjectMembershipEntity projectMembershipEntity : projectMembershipEntities) {
            if (projectMembershipEntity.getRole().equals(ProjectRoleEnum.PROJECT_MANAGER)) {
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
                notificationEntity.setUser(projectMembershipEntity.getUser());
                notificationEntity.setDateTime(Instant.now());
                notificationEntity.setRead(false);
                notificationEntity.setContent("User " + projectMembershipEn.getUser().getUsername() + " has requested to join project " + projectMembershipEn.getProject().getName() + " please confirm or reject on your email");
                notificationEntity.setProject(projectMembershipEn.getProject());
                LOGGER.info("Creating notification for project join request");
                System.out.println("Creating notification for project join request");
                notificationDao.persist(notificationEntity);
                NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
                GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
            }
        }
    }
    public void createNotificationForProjectJoinRequestApprovedOrRejected(ProjectMembershipEntity projectMembershipEntity, boolean approved) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        if (approved) {
            notificationEntity.setContent("Your request to join project " + projectMembershipEntity.getProject().getName() + " has been approved");
        } else {
            notificationEntity.setContent("Your request to join project " + projectMembershipEntity.getProject().getName() + " has been rejected");
        }
        notificationEntity.setProject(projectMembershipEntity.getProject());
        LOGGER.info("Creating notification for project join request approved");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createNotificationForProjectInviteFromAProjectManagerToUser(ProjectMembershipEntity projectMembershipEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setContent("You have been invited to join project " + projectMembershipEntity.getProject().getName() + " please confirm or reject on your email");
        notificationEntity.setProject(projectMembershipEntity.getProject());
        LOGGER.info("Creating notification for project invite from a project manager to user");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createNotificationForProjectManagersKnowUserApproval(ProjectMembershipEntity projectMembershipEntity, boolean approved) {
        Set<ProjectMembershipEntity> projectMembershipEntitiesSet = projectMembershipEntity.getProject().getMembers();
        List<ProjectMembershipEntity> projectMembershipEntities = new ArrayList<>(projectMembershipEntitiesSet);

        for (ProjectMembershipEntity projectMembershipEntity1 : projectMembershipEntities) {
            if (projectMembershipEntity1.getRole().equals(ProjectRoleEnum.PROJECT_MANAGER)) {
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
                notificationEntity.setUser(projectMembershipEntity1.getUser());
                notificationEntity.setDateTime(Instant.now());
                notificationEntity.setRead(false);
                if (approved) {
                    notificationEntity.setContent("User " + projectMembershipEntity.getUser().getUsername() + " approved the invitation to join " + projectMembershipEntity.getProject().getName());
                } else {
                    notificationEntity.setContent("User " + projectMembershipEntity.getUser().getUsername() + " rejected the invitation to join " + projectMembershipEntity.getProject().getName());
                }
                notificationEntity.setProject(projectMembershipEntity.getProject());
                LOGGER.info("Creating notification for project managers know user approval");
                notificationDao.persist(notificationEntity);
                NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
                GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
            }
        }
    }
    public void createNotificationForUserRemovedFromProject(ProjectMembershipEntity projectMembershipEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        if(projectMembershipEntity.isAccepted())notificationEntity.setContent("You have been removed from project " + projectMembershipEntity.getProject().getName());
        else notificationEntity.setContent("Your invitation to join project " + projectMembershipEntity.getProject().getName() + " has been cancelled");
        notificationEntity.setProject(projectMembershipEntity.getProject());
        LOGGER.info("Creating notification for user removed from project");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createNotificationForUserAutomaticallyAddedToProject(ProjectMembershipEntity projectMembershipEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setContent("You have been automatically added to project " + projectMembershipEntity.getProject().getName());
        notificationEntity.setProject(projectMembershipEntity.getProject());
        LOGGER.info("Creating notification for user automatically added to project");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createNotificationProjectApprovalSendAllMembers(ProjectEntity projectEntity, UserEntity userEntity, boolean approved) {
        Set<ProjectMembershipEntity> projectMembershipEntitiesSet = projectEntity.getMembers();
        List<ProjectMembershipEntity> projectMembershipEntities = new ArrayList<>(projectMembershipEntitiesSet);

        for (ProjectMembershipEntity projectMembershipEntity : projectMembershipEntities) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType(NotificationTypeENUM.PROJECT_APPROVAL);
            notificationEntity.setUser(projectMembershipEntity.getUser());
            notificationEntity.setDateTime(Instant.now());
            notificationEntity.setRead(false);
            if (approved) {
                notificationEntity.setContent("Project " + projectEntity.getName() + " has been approved by " + userEntity.getUsername());
            } else {
                notificationEntity.setContent("Project " + projectEntity.getName() + " has been rejected by " + userEntity.getUsername());
            }
            notificationEntity.setProject(projectEntity);
            LOGGER.info("Creating notification for project approval send all members");
            notificationDao.persist(notificationEntity);
            NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
            GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
        }
    }
    public void createNotificationForAllPlatformAdminsProjectApproval(ProjectEntity projectEntity) {
        RoleEntity role  = roleDao.findRoleByName(UserRoleEnum.ADMIN);
        List<UserEntity> platformAdmins = userDao.getUsersByRole(role);
        for (UserEntity platformAdmin : platformAdmins) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType(NotificationTypeENUM.PROJECT_APPROVAL);
            notificationEntity.setUser(platformAdmin);
            notificationEntity.setDateTime(Instant.now());
            notificationEntity.setRead(false);
            notificationEntity.setContent("Project " + projectEntity.getName() + " is ready to be approved. Visit project page to approve or reject.");

            notificationEntity.setProject(projectEntity);
            LOGGER.info("Creating notification for all platform admins project approval");
            notificationDao.persist(notificationEntity);
            NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
            GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
        }
    }
    public void createNotificationMarkesAsResponsibleInNewTask(UserEntity userEntity, TaskEntity taskEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.TASK_RESPONSIBLE);
        notificationEntity.setUser(userEntity);
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setTask(taskEntity);
        notificationEntity.setContent("You have been marked as responsible in the new task " + taskEntity.getTitle() + " in project " + taskEntity.getProject().getName());
        LOGGER.info("Creating notification for marked as responsible in new task");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }
    public void createNotificationMarkesAsExecutorInNewTask(UserEntity userEntity, TaskEntity taskEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.TASK_EXECUTER);
        notificationEntity.setUser(userEntity);
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setTask(taskEntity);
        notificationEntity.setContent("You have been marked as executor in the task " + taskEntity.getTitle() + " in project " + taskEntity.getProject().getName());
        LOGGER.info("Creating notification for marked as executor in new task");
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntetyToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }


    public List<NotificationGetDto> getUnreadNotifications(SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        List<NotificationEntity> notificationsEntety =  notificationDao.getUnreadbByUserNotifications(authUserDto.getUserId());
        return convertEntetiesToDtos(notificationsEntety);
    }

    public void markNotificationsAsRead(Long notificationId){
        NotificationEntity notificationEntity = notificationDao.findNotificationById(notificationId);
        notificationEntity.setRead(true);
        notificationDao.merge(notificationEntity);
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
        if(notificationEntity.getProject() != null)notificationGetDto.setProjectId(notificationEntity.getProject().getId());
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