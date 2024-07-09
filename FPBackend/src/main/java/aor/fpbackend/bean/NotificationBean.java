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
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * EJB Stateless bean responsible for managing notifications within the application.
 * Handles creation of various types of notifications including individual messages, project join requests,
 * task assignments, group messages, and project approvals/rejections.
 * Provides methods for marking notifications as read and retrieving unread notifications for users.
 */
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

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(NotificationBean.class);


    /**
     * Creates a notification for an individual message and persists it.
     * Sends the notification to the recipient's user sessions using WebSocket.
     *
     * @param messageEntity The entity representing the individual message.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createIndividualMessageNotification(IndividualMessageEntity messageEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.INDIVIDUAL_MESSAGE);
        notificationEntity.setUser(messageEntity.getRecipient());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setIndividualMessage(messageEntity);
        notificationEntity.setContent("You have a new message from " + messageEntity.getSender().getUsername());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);

    }

    /**
     * Creates notification(s) for project administrators when a user requests to join their project.
     * Each project manager/administrator receives a notification if the requesting user's role is
     * set as 'PROJECT_MANAGER'.
     *
     * @param projectMembershipEnt The entity representing the membership request.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createProjectJoinRequestNotificationsForProjectAdmins(ProjectMembershipEntity projectMembershipEnt) throws UnknownHostException {
        Set<ProjectMembershipEntity> projectMembershipEntitiesSet = projectMembershipEnt.getProject().getMembers();
        List<ProjectMembershipEntity> projectMembershipEntities = new ArrayList<>(projectMembershipEntitiesSet);
        for (ProjectMembershipEntity projectMembershipEntity : projectMembershipEntities) {
            if (projectMembershipEntity.getRole().equals(ProjectRoleEnum.PROJECT_MANAGER)) {
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
                notificationEntity.setUser(projectMembershipEntity.getUser());
                notificationEntity.setDateTime(Instant.now());
                notificationEntity.setRead(false);
                notificationEntity.setContent("User " + projectMembershipEnt.getUser().getUsername() + " has requested to join project " + projectMembershipEnt.getProject().getName() + " please confirm or reject on your email");
                notificationEntity.setProject(projectMembershipEnt.getProject());
                notificationDao.persist(notificationEntity);
                NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
                GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
                LOGGER.info("Creating notification for project join request");
            }
        }
    }

    /**
     * Creates a notification for a user whose project join request has been approved or rejected.
     *
     * @param projectMembershipEntity The entity representing the project membership request.
     * @param approved                Boolean indicating whether the request was approved (true) or rejected (false).
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationForProjectJoinRequestApprovedOrRejected(ProjectMembershipEntity projectMembershipEntity, boolean approved) throws UnknownHostException {
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
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Creates a notification for a user who has been invited by a project manager to join a project.
     *
     * @param projectMembershipEntity The entity representing the project membership invitation.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationForProjectInviteFromAProjectManagerToUser(ProjectMembershipEntity projectMembershipEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setContent("You have been invited to join project " + projectMembershipEntity.getProject().getName() + " please confirm or reject on your email");
        notificationEntity.setProject(projectMembershipEntity.getProject());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Creates a notification for project managers informing them about user approval or rejection for project membership.
     *
     * @param projectMembershipEntity The entity representing the project membership.
     * @param approved                Indicates whether the user's request to join the project was approved.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationForProjectManagersKnowUserApproval(ProjectMembershipEntity projectMembershipEntity, boolean approved) throws UnknownHostException {
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
                notificationDao.persist(notificationEntity);
                NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
                GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
            }
        }
    }

    public void createNotificationForUserRemovedFromProject(ProjectMembershipEntity projectMembershipEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        if (projectMembershipEntity.isAccepted())
            notificationEntity.setContent("You have been removed from project " + projectMembershipEntity.getProject().getName());
        else
            notificationEntity.setContent("Your invitation to join project " + projectMembershipEntity.getProject().getName() + " has been cancelled");
        notificationEntity.setProject(projectMembershipEntity.getProject());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Creates a notification for a user who has been removed from a project or whose invitation to join a project has been cancelled.
     *
     * @param projectMembershipEntity The entity representing the project membership.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationForUserAutomaticallyAddedToProject(ProjectMembershipEntity projectMembershipEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.PROJECT_JOIN_REQUEST);
        notificationEntity.setUser(projectMembershipEntity.getUser());
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setContent("You have been automatically added to project " + projectMembershipEntity.getProject().getName());
        notificationEntity.setProject(projectMembershipEntity.getProject());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Creates notifications for all members of a project about its approval or rejection by a user.
     *
     * @param projectEntity The entity representing the project.
     * @param userEntity    The entity representing the user who approved or rejected the project.
     * @param approved      A boolean indicating whether the project was approved (true) or rejected (false).
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationProjectApprovalSendAllMembers(ProjectEntity projectEntity, UserEntity userEntity, boolean approved) throws UnknownHostException {
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
            notificationDao.persist(notificationEntity);
            NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
            GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
        }
    }

    /**
     * Creates notifications for all platform administrators about a project awaiting approval.
     *
     * @param projectEntity The entity representing the project awaiting approval.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationForAllPlatformAdminsProjectApproval(ProjectEntity projectEntity) throws UnknownHostException {
        RoleEntity role = roleDao.findRoleByName(UserRoleEnum.ADMIN);
        List<UserEntity> platformAdmins = userDao.getUsersByRole(role);
        for (UserEntity platformAdmin : platformAdmins) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType(NotificationTypeENUM.PROJECT_APPROVAL);
            notificationEntity.setUser(platformAdmin);
            notificationEntity.setDateTime(Instant.now());
            notificationEntity.setRead(false);
            notificationEntity.setContent("Project " + projectEntity.getName() + " is ready to be approved. Visit project page to approve or reject.");
            notificationEntity.setProject(projectEntity);
            notificationDao.persist(notificationEntity);
            NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
            GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
        }
    }

    /**
     * Creates a notification for a user who has been marked as responsible in a new task.
     *
     * @param userEntity The user entity who is marked as responsible.
     * @param taskEntity The task entity in which the user is marked as responsible.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationMarksAsResponsibleInNewTask(UserEntity userEntity, TaskEntity taskEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.TASK_RESPONSIBLE);
        notificationEntity.setUser(userEntity);
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setTask(taskEntity);
        notificationEntity.setProject(taskEntity.getProject());
        notificationEntity.setContent("You have been marked as responsible in the new task " + taskEntity.getTitle() + " in project " + taskEntity.getProject().getName());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Creates a notification for a user who has been marked as executor in a new task.
     *
     * @param userEntity The user entity who is marked as executor.
     * @param taskEntity The task entity in which the user is marked as executor.
     * @throws UnknownHostException If the host IP address cannot be determined.
     */
    public void createNotificationMarksAsExecutorInNewTask(UserEntity userEntity, TaskEntity taskEntity) throws UnknownHostException {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType(NotificationTypeENUM.TASK_EXECUTER);
        notificationEntity.setUser(userEntity);
        notificationEntity.setDateTime(Instant.now());
        notificationEntity.setRead(false);
        notificationEntity.setTask(taskEntity);
        notificationEntity.setContent("You have been marked as executor in the task " + taskEntity.getTitle() + " in project " + taskEntity.getProject().getName());
        notificationDao.persist(notificationEntity);
        NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
        GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
    }

    /**
     * Retrieves unread notifications for the authenticated user.
     *
     * @param securityContext The security context containing the authenticated user's principal.
     * @return A list of NotificationGetDto objects representing unread notifications.
     */
    public List<NotificationGetDto> getUnreadNotifications(SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        List<NotificationEntity> notificationsEntity = notificationDao.getUnreadbByUserNotifications(authUserDto.getUserId());
        return convertEntitiesToDtos(notificationsEntity);
    }

    /**
     * Marks a notification as read by setting its 'read' flag to true.
     *
     * @param notificationId The ID of the notification to mark as read.
     */
    public void markNotificationsAsRead(Long notificationId) throws EntityNotFoundException {
        NotificationEntity notificationEntity = notificationDao.findNotificationById(notificationId);
        if (notificationEntity == null) {
            throw new EntityNotFoundException("No notification found for this Id");
        }
        notificationEntity.setRead(true);
        notificationDao.merge(notificationEntity);
    }

    /**
     * Converts a list of NotificationEntity objects to a list of NotificationGetDto objects.
     *
     * @param notificationsEntity The list of NotificationEntity objects to convert.
     * @return The list of NotificationGetDto objects converted from NotificationEntity objects.
     */
    private List<NotificationGetDto> convertEntitiesToDtos(List<NotificationEntity> notificationsEntity) {
        List<NotificationGetDto> notificationGetDtos = new ArrayList<>();
        for (NotificationEntity notificationEntity : notificationsEntity) {
            NotificationGetDto newDto = convertEntityToDto(notificationEntity);
            notificationGetDtos.add(newDto);
        }
        return notificationGetDtos;
    }

    /**
     * Converts a NotificationEntity object to a NotificationGetDto object.
     *
     * @param notificationEntity The NotificationEntity object to convert.
     * @return The converted NotificationGetDto object.
     */
    private NotificationGetDto convertEntityToDto(NotificationEntity notificationEntity) {
        NotificationGetDto notificationGetDto = new NotificationGetDto();
        notificationGetDto.setId(notificationEntity.getId());
        notificationGetDto.setType(notificationEntity.getType());
        notificationGetDto.setContent(notificationEntity.getContent());
        notificationGetDto.setDateTime(notificationEntity.getDateTime());
        notificationGetDto.setRead(notificationEntity.isRead());
        notificationGetDto.setUser(userBean.convertUserEntitytoUserBasicInfoDto(notificationEntity.getUser()));
        if (notificationEntity.getProject() != null)
            notificationGetDto.setProjectId(notificationEntity.getProject().getId());
        if (notificationEntity.getIndividualMessage() != null)
            notificationGetDto.setIndividualMessage(new IndividualMessageGetDto(
                    notificationEntity.getIndividualMessage().getId(),
                    notificationEntity.getIndividualMessage().getContent(),
                    userBean.convertUserEntitytoUserBasicInfoDto(notificationEntity.getIndividualMessage().getSender()),
                    userBean.convertUserEntitytoUserBasicInfoDto(notificationEntity.getIndividualMessage().getRecipient()),
                    notificationEntity.getIndividualMessage().getSubject(),
                    notificationEntity.getIndividualMessage().getSentTime(),
                    notificationEntity.getIndividualMessage().isViewed()
            ));
        return notificationGetDto;
    }

    /**
     * Creates notifications for a group message sent to project members.
     *
     * @param groupMessageEntity The GroupMessageEntity containing the group message details.
     * @param projectMembers     The list of project members to receive the notification.
     * @throws UnknownHostException If there is an issue with host resolution.
     */
    public void createNotificationForGroupMessage(GroupMessageEntity groupMessageEntity, List<UserEntity> projectMembers) {
        if (projectMembers == null) {
            throw new IllegalArgumentException("Project members list is null");
        }
        for (UserEntity userEntity : projectMembers) {
            if (!userEntity.equals(groupMessageEntity.getSender())) {
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setType(NotificationTypeENUM.GROUP_MESSAGE);
                notificationEntity.setUser(userEntity);
                notificationEntity.setDateTime(Instant.now());
                notificationEntity.setRead(false);
                notificationEntity.setContent("You have a new message in group " + groupMessageEntity.getGroup().getName() + " from " + groupMessageEntity.getSender().getUsername());
                notificationEntity.setGroupMessage(groupMessageEntity);
                notificationEntity.setProject(groupMessageEntity.getGroup());
                notificationDao.persist(notificationEntity);
                NotificationGetDto notificationGetDto = convertEntityToDto(notificationEntity);
                GlobalWebSocket.tryToSendNotificationToUserSessions(notificationGetDto);
                LOGGER.info("Creating notification for group message");
            }
        }
    }

}
