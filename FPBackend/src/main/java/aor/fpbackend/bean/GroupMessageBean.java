package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.GroupMessageGetDto;
import aor.fpbackend.dto.GroupMessageSendDto;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@Stateless
public class GroupMessageBean {
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
        return null;
    }

}