package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
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


    @Transactional
    public void sendGroupMessage(GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException {
        // Find the sender user by id
        UserEntity senderUser = userDao.findUserById(groupMessageSendDto.getSenderId());
        if (senderUser == null) {
            throw new UserNotFoundException("No user found for this id");
        }
        // Find the project entity based on groupId
        ProjectEntity projectEntity = projectDao.findProjectById(groupMessageSendDto.getGroupId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with this Id");
        }
        // Retrieve all project members based on groupId
        List<UserEntity> projectMembers = projectMemberDao.findProjectMembersByProjectId(groupMessageSendDto.getGroupId());
        if (projectMembers == null || projectMembers.isEmpty()) {
            throw new UserNotFoundException("Invalid groupId or no members found for the project");
        }
        // Create and persist group messages for each project member
        for (UserEntity member : projectMembers) {
            GroupMessageEntity groupMessageEntity = createGroupMessageEntity(groupMessageSendDto.getContent(), member, projectEntity);
            groupMessageDao.persist(groupMessageEntity);
        }
    }

    private GroupMessageEntity createGroupMessageEntity(String messageContent, UserEntity recipient, ProjectEntity projectEntity) {
        GroupMessageEntity groupMessageEntity = new GroupMessageEntity();
        groupMessageEntity.setContent(messageContent);
        groupMessageEntity.setSender(recipient);
        groupMessageEntity.setSentTime(Instant.now());
        groupMessageEntity.setViewed(false);
        groupMessageEntity.setGroup(projectEntity);
        return groupMessageEntity;
    }

    public List<GroupMessageGetDto> getGroupMessages(long projectId) {
        List<GroupMessageEntity> groupMessageEntities = groupMessageDao.getGroupMessagesByProjectId(projectId);
        List<GroupMessageGetDto> groupMessageGetDtos = convertGroupMessageEntityListToGroupMessageGetDtoList(groupMessageEntities);
        return groupMessageGetDtos;
    }

    public void markMessageAsRead(GroupMessageMarkReadDto groupMessageMarkReadDto) {
        List<GroupMessageEntity> previousMessages = groupMessageDao.findPreviousGroupMessages(groupMessageMarkReadDto.getGroupId(), groupMessageMarkReadDto.getSentTime());
        // Mark all previous messages as read
        for (GroupMessageEntity previousMessage : previousMessages) {
            previousMessage.setViewed(true);
        }
        LOGGER.warn("Group Messages marked as read until " + groupMessageMarkReadDto.getSentTime());
    }

    public GroupMessageGetDto convertGroupMessageEntityToGroupMessageGetDto(GroupMessageEntity groupMessageEntity) {
        GroupMessageGetDto groupMessageGetDto = new GroupMessageGetDto();
        groupMessageGetDto.setId(groupMessageEntity.getId());
        groupMessageGetDto.setContent(groupMessageEntity.getContent());
        groupMessageGetDto.setSenderId(groupMessageEntity.getSender().getId());
        groupMessageGetDto.setSentTime(groupMessageEntity.getSentTime());
        groupMessageGetDto.setViewed(groupMessageEntity.isViewed());
        groupMessageGetDto.setGroupId(groupMessageEntity.getGroup().getId());
        return groupMessageGetDto;
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