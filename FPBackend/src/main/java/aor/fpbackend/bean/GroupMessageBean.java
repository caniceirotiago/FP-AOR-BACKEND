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
            throw new UserNotFoundException("User with Id: " + groupMessageSendDto.getSenderId() + " not found");
        }
        // Find the project entity based on groupId
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

    public List<GroupMessageGetDto> getGroupMessages(long projectId) {
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getGroupMessagesByProjectId(projectId);
        List<GroupMessageGetDto> groupMessageGetDtos = convertGroupMessageEntityListToGroupMessageGetDtoList(groupMessageEntities);
        return groupMessageGetDtos;
    }

    public void markMessageAsRead(GroupMessageMarkReadDto groupMessageMarkReadDto, SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Find the authenticated sender user by their ID
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User with Id: " + authUserDto.getUserId() + " not found");
        }
        GroupMessageEntity messageEntity = groupMessageDao.findGroupMessageById(groupMessageMarkReadDto.getMessageId());
        if (messageEntity == null) {
            throw new EntityNotFoundException("Message not found with this Id");
        }
        messageEntity.getReadByUsers().add(userEntity);
        // Check if all users in the group have read the message
        List<UserEntity> groupMembers = projectMemberDao.findProjectMembersByProjectId(groupMessageMarkReadDto.getGroupId());
        if (groupMembers.stream().allMatch(user -> messageEntity.getReadByUsers().contains(user))) {
            messageEntity.setViewed(true);
            LOGGER.warn("Group Messages marked as read");
        }
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