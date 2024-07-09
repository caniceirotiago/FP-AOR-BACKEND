package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.GroupMessage.GroupMessageGetDto;
import aor.fpbackend.dto.GroupMessage.GroupMessageSendDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class GroupMessageBean {

    private static final long serialVersionUID = 1L;

    @EJB
    UserBean userBean;
    @EJB
    UserDao userDao;
    @EJB
    GroupMessageDao groupMessageDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    ProjectDao projectDao;

    /**
     * Sends a group message from a sender to a project group.
     *
     * @param groupMessageSendDto The DTO containing the sender ID, group ID, and message content.
     * @return The GroupMessageEntity representing the sent group message.
     * @throws UserNotFoundException   If no user is found with the specified sender ID.
     * @throws EntityNotFoundException If no project is found with the specified group ID.
     */
    public GroupMessageEntity sendGroupMessage(GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException {
        // Find the sender user by their ID
        UserEntity senderEntity = userDao.findUserById(groupMessageSendDto.getSenderId());
        if (senderEntity == null) {
            throw new UserNotFoundException("Esta a falhar no bean com User with Id: " + groupMessageSendDto.getSenderId() + " not found");
        }
        // Find the project entity based on groupId
        ProjectEntity projectEntity = projectDao.findProjectById(groupMessageSendDto.getGroupId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with this Id");
        }
        // Create the GroupMessageEntity using provided data and persist it
        GroupMessageEntity groupMessageEntity = createGroupMessageEntity(groupMessageSendDto.getContent(), senderEntity, projectEntity);
        groupMessageDao.persist(groupMessageEntity);
        // Mark the sender as having read the message
        groupMessageEntity.getReadByUsers().add(senderEntity);
        // Add the GroupMessageEntity to the project's groupMessages set
        projectEntity.getGroupMessages().add(groupMessageEntity);
        return groupMessageEntity;
    }

    /**
     * Creates a new GroupMessageEntity using the provided message content, sender, and project entity.
     *
     * @param messageContent The content of the group message.
     * @param sender         The UserEntity representing the sender of the message.
     * @param projectEntity  The ProjectEntity to which the message belongs.
     * @return The newly created GroupMessageEntity initialized with the provided data.
     */
    private GroupMessageEntity createGroupMessageEntity(String messageContent, UserEntity sender, ProjectEntity projectEntity) {
        GroupMessageEntity groupMessageEntity = new GroupMessageEntity();
        groupMessageEntity.setContent(messageContent);
        groupMessageEntity.setSender(sender);
        groupMessageEntity.setSentTime(Instant.now());
        groupMessageEntity.setGroup(projectEntity);
        groupMessageEntity.setViewed(false);
        return groupMessageEntity;
    }

    /**
     * Retrieves all group messages associated with a project identified by projectId.
     *
     * @param projectId       The ID of the project for which group messages are to be retrieved.
     * @param securityContext The security context providing access to the authenticated user.
     * @return A list of GroupMessageGetDto objects representing group messages for the specified project.
     * @throws UserNotFoundException If no user is found for the authenticated user ID.
     */
    public List<GroupMessageGetDto> getGroupMessagesByProjectId(long projectId, SecurityContext securityContext) throws UserNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("No user found for this Id");
        }
        // Retrieve group message entities associated with the specified project ID
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getGroupMessagesByProjectId(projectId);
        // Convert GroupMessageEntity objects to GroupMessageGetDto objects
        List<GroupMessageGetDto> groupMessageGetDtos = convertGroupMessageEntityListToGroupMessageGetDtoList(groupMessageEntities);
        return groupMessageGetDtos;
    }

    /**
     * Retrieves group messages based on a list of message IDs.
     *
     * @param messageIds The list of message IDs to retrieve group messages from.
     * @return A list of GroupMessageGetDto objects representing group messages for the specified message IDs.
     */
    public List<GroupMessageGetDto> getGroupMessagesByMessageIds(List<Long> messageIds) {
        // Retrieve group message entities based on the provided message IDs
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getMessagesByIds(messageIds);
        // Convert GroupMessageEntity objects to GroupMessageGetDto objects
        return groupMessageEntities.stream()
                .map(this::convertGroupMessageEntityToGroupMessageGetDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks a group message as read by a specific user.
     *
     * @param messageId The ID of the group message to mark as read.
     * @param userId    The ID of the user who is marking the message as read.
     */
    public void markMessageAsReadByUser(Long messageId, Long userId) {
        // Find the group message entity by its ID
        GroupMessageEntity message = groupMessageDao.findGroupMessageById(messageId);
        // Find the user entity by its ID
        UserEntity user = userDao.findUserById(userId);
        // If both the message and user entities are found
        if (message != null && user != null) {
            // Add the user to the list of users who have read the message
            message.getReadByUsers().add(user);
            // Check if all users have read the message, and mark it as viewed if true
            if (allUsersHaveReadMessage(message.getId())) {
                message.setViewed(true);
            }
        }
    }

    /**
     * Checks if all users associated with a group message have read the message.
     *
     * @param messageId The ID of the group message to check.
     * @return true if all users associated with the message have read it, false otherwise.
     */
    private boolean allUsersHaveReadMessage(Long messageId) {
        // Find the group message entity by its ID
        GroupMessageEntity messageEntity = groupMessageDao.findGroupMessageById(messageId);
        // If the message entity is found
        if (messageEntity != null) {
            // Get the set of users who have read the message
            Set<UserEntity> readByUsers = messageEntity.getReadByUsers();
            // Get the list of users associated with the group (project members)
            List<UserEntity> groupUsers = projectMemberDao.findProjectMembersByProjectId(messageEntity.getGroup().getId());
            // Check if all group users have read the message
            return readByUsers.containsAll(groupUsers);
        }
        return false;
    }

    /**
     * Verifies if all specified messages are marked as read for a user.
     *
     * @param messageIds The list of message IDs to verify as read.
     * @param userId     The ID of the user to verify message readings for.
     * @return true if all specified messages are marked as read by the user, false otherwise.
     */
    public boolean verifyMessagesAsReadForGroup(List<Long> messageIds, Long userId) {
        boolean allMarkedAsRead = true;
        // Iterate through each message ID in the list
        for (Long messageId : messageIds) {
            // Mark the message as read by the user
            markMessageAsReadByUser(messageId, userId);
            // Check if all users have read the message
            boolean markedAsRead = allUsersHaveReadMessage(messageId);
            // Update allMarkedAsRead based on whether the message is marked as read by all users
            if (!markedAsRead) {
                allMarkedAsRead = false;
            }
        }
        return allMarkedAsRead;
    }

    /**
     * Converts a GroupMessageEntity to a GroupMessageGetDto.
     *
     * @param groupMessageEntity The GroupMessageEntity to convert.
     * @return A GroupMessageGetDto representing the converted GroupMessageEntity.
     */
    public GroupMessageGetDto convertGroupMessageEntityToGroupMessageGetDto(GroupMessageEntity groupMessageEntity) {
        // Extracts the IDs of users who have read the message
        Set<Long> readByUserIds = groupMessageEntity.getReadByUsers().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
        // Constructs and returns a new GroupMessageGetDto based on the GroupMessageEntity
        return new GroupMessageGetDto(
                groupMessageEntity.getId(),
                groupMessageEntity.getContent(),
                userBean.convertUserEntitytoUserBasicInfoDto(groupMessageEntity.getSender()),
                groupMessageEntity.getSentTime(),
                readByUserIds,
                groupMessageEntity.isViewed(),
                groupMessageEntity.getGroup().getId()
        );
    }

    /**
     * Converts a list of GroupMessageEntity objects to a list of GroupMessageGetDto objects.
     *
     * @param groupMessageEntities The list of GroupMessageEntity objects to convert.
     * @return A list of GroupMessageGetDto objects representing the converted GroupMessageEntity objects.
     */
    public List<GroupMessageGetDto> convertGroupMessageEntityListToGroupMessageGetDtoList(List<GroupMessageEntity> groupMessageEntities) {
        // Initialize an empty list to store converted GroupMessageGetDto objects
        List<GroupMessageGetDto> groupMessageGetDtos = new ArrayList<>();
        // Iterate through each GroupMessageEntity in the input list
        for (GroupMessageEntity groupMessageEntity : groupMessageEntities) {
            // Convert each GroupMessageEntity to a GroupMessageGetDto and add to the result list
            GroupMessageGetDto groupMessageGetDto = convertGroupMessageEntityToGroupMessageGetDto(groupMessageEntity);
            groupMessageGetDtos.add(groupMessageGetDto);
        }
        // Return the list of converted GroupMessageGetDto objects
        return groupMessageGetDtos;
    }
}