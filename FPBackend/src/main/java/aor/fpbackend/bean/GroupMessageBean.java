package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
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
    UserDao userDao;
    @EJB
    GroupMessageDao groupMessageDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    ProjectDao projectDao;


    public void sendGroupMessage(GroupMessageSendDto groupMessageSendDto, SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Find the authenticated sender user by their ID
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity senderEntity = userDao.findUserById(authUserDto.getUserId());
        if (senderEntity == null) {
            throw new UserNotFoundException("User with Id: " + authUserDto.getUserId() + " not found");
        }
        // Find the project entity based on groupId
        ProjectEntity projectEntity = projectDao.findProjectById(groupMessageSendDto.getGroupId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with this Id");
        }
        // Retrieve all project members based on groupId
//        List<UserEntity> projectMembers = projectMemberDao.findProjectMembersByProjectId(groupMessageSendDto.getGroupId());
//        if (projectMembers == null || projectMembers.isEmpty()) {
//            throw new UserNotFoundException("Invalid groupId or no members found for the project");
//        }
        GroupMessageEntity groupMessageEntity = createGroupMessageEntity(groupMessageSendDto.getContent(), senderEntity, projectEntity);
        groupMessageDao.persist(groupMessageEntity);
        groupMessageEntity.getReadByUsers().add(senderEntity); // Mark the sender as having read the message
        projectEntity.getGroupMessages().add(groupMessageEntity);
    }

    private GroupMessageEntity createGroupMessageEntity(String messageContent, UserEntity sender, ProjectEntity projectEntity) {
        GroupMessageEntity groupMessageEntity = new GroupMessageEntity();
        groupMessageEntity.setContent(messageContent);
        groupMessageEntity.setSender(sender);
        groupMessageEntity.setSentTime(Instant.now());
        groupMessageEntity.setGroup(projectEntity);
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
                groupMessageEntity.getSender().getId(),
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