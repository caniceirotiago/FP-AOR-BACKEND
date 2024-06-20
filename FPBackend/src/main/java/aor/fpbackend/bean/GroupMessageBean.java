package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.websocket.GroupMessageWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class GroupMessageBean {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(UserBean.class);

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


    public GroupMessageEntity sendGroupMessage(GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException {
        // Find the sender user by their ID
        UserEntity senderEntity = userDao.findUserById(groupMessageSendDto.getSenderId());
        if (senderEntity == null) {
            throw new UserNotFoundException("Esta a falhar no bean com User with Id: " + groupMessageSendDto.getSenderId() + " not found");
        }
        // Find the project entity based on groupId\
        ProjectEntity projectEntity = projectDao.findProjectById(groupMessageSendDto.getGroupId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with this Id");
        }
        GroupMessageEntity groupMessageEntity = createGroupMessageEntity(groupMessageSendDto.getContent(), senderEntity, projectEntity);
        groupMessageDao.persist(groupMessageEntity);
        groupMessageEntity.getReadByUsers().add(senderEntity); // Mark the sender as having read the message
        projectEntity.getGroupMessages().add(groupMessageEntity);
        return groupMessageEntity;
    }

    private GroupMessageEntity createGroupMessageEntity(String messageContent, UserEntity sender, ProjectEntity projectEntity) {
        GroupMessageEntity groupMessageEntity = new GroupMessageEntity();
        groupMessageEntity.setContent(messageContent);
        groupMessageEntity.setSender(sender);
        groupMessageEntity.setSentTime(Instant.now());
        groupMessageEntity.setGroup(projectEntity);
        groupMessageEntity.setViewed(false);
        return groupMessageEntity;
    }

    public List<GroupMessageGetDto> getGroupMessagesByProjectId(long projectId, SecurityContext securityContext) throws UserNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("No user found for this Id");
        }
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getGroupMessagesByProjectId(projectId);
        for (GroupMessageEntity groupMessageEntity : groupMessageEntities) {
            markMessageAsReadByUser(groupMessageEntity.getId(), userEntity.getId());
        }
        List<GroupMessageGetDto> groupMessageGetDtos = convertGroupMessageEntityListToGroupMessageGetDtoList(groupMessageEntities);
        return groupMessageGetDtos;
    }

    public List<GroupMessageGetDto> getGroupMessagesByMessageIds(List<Long> messageIds) {
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getMessagesByIds(messageIds);
        return groupMessageEntities.stream()
                .map(this::convertGroupMessageEntityToGroupMessageGetDto)
                .collect(Collectors.toList());

    }

    public void markMessageAsReadByUser(Long messageId, Long userId) {
        GroupMessageEntity message = groupMessageDao.findGroupMessageById(messageId);
        UserEntity user = userDao.findUserById(userId);
        if (message != null && user != null) {
            message.getReadByUsers().add(user);
            if (allUsersHaveReadMessage(message.getId())) {
                message.setViewed(true);
            }
        }
    }

    private boolean allUsersHaveReadMessage(Long messageId) {
        GroupMessageEntity messageEntity = groupMessageDao.findGroupMessageById(messageId);
        if (messageEntity != null) {
            Set<UserEntity> readByUsers = messageEntity.getReadByUsers();
            List<UserEntity> groupUsers = projectMemberDao.findProjectMembersByProjectId(messageEntity.getGroup().getId());
            return readByUsers.containsAll(groupUsers);
        }
        return false;
    }

    public boolean verifyMessagesAsReadForGroup(List<Long> messageIds, Long userId) {
        boolean allMarkedAsRead = true;
        for (Long messageId : messageIds) {
            // Check if all users have read the message
            markMessageAsReadByUser(messageId, userId);
            boolean markedAsRead = allUsersHaveReadMessage(messageId);
            if (!markedAsRead) {
                allMarkedAsRead = false;
            }
        }
        return allMarkedAsRead;
    }

    public GroupMessageGetDto convertGroupMessageEntityToGroupMessageGetDto(GroupMessageEntity groupMessageEntity) {
        return new GroupMessageGetDto(
                groupMessageEntity.getId(),
                groupMessageEntity.getContent(),
                userBean.convertUserEntetyToUserBasicInfoDto(groupMessageEntity.getSender()),
                groupMessageEntity.getSentTime(),
                groupMessageEntity.isViewed(),
                groupMessageEntity.getGroup().getId()
        );
    }

    public List<GroupMessageGetDto> convertGroupMessageEntityListToGroupMessageGetDtoList(List<GroupMessageEntity> groupMessageEntities) {
        List<GroupMessageGetDto> groupMessageGetDtos = new ArrayList<>();
        for (GroupMessageEntity groupMessageEntity : groupMessageEntities) {
            GroupMessageGetDto groupMessageGetDto = convertGroupMessageEntityToGroupMessageGetDto(groupMessageEntity);
            groupMessageGetDtos.add(groupMessageGetDto);
        }
        return groupMessageGetDtos;
    }
}