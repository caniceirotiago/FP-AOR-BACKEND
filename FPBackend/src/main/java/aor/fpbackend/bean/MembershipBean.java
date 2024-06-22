package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.LogTypeEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;

@Stateless
public class MembershipBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(MembershipBean.class);

    @EJB
    EmailService emailService;
    @EJB
    UserDao userDao;
    @EJB
    ProjectDao projectDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    ProjectBean projectBean;
    @EJB
    ConfigurationBean configurationBean;
    @EJB
    SessionBean sessionBean;
    @EJB
    NotificationBean notificationBean;


    public void askToJoinProject(long projectId, SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        // Check if the current number of project members has reached the maximum limit
        int maxProjectElements = configurationBean.getConfigValueByKey("maxProjectMembers");
        if (projectEntity.getMembers().size() >= maxProjectElements) {
            throw new IllegalStateException("Project member's limit is reached");
        }
        // Check if User is already a project member
        for (ProjectMembershipEntity pm : projectEntity.getMembers()) {
            if (pm.getUser().getId() == userEntity.getId()) {
                throw new DuplicatedAttributeException("User is already member of the project");
            }
        }
        if (projectEntity != null && userEntity != null) {
            ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
            projectMembershipEntity.setProject(projectEntity);
            projectMembershipEntity.setUser(userEntity);
            projectMembershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
            projectMembershipEntity.setAccepted(false);
            projectMembershipEntity.setAcceptanceToken(UUID.randomUUID().toString());
            projectMemberDao.persist(projectMembershipEntity);
            sendJoinRequisitionToManagers(projectMembershipEntity, userEntity, projectEntity);
            notificationBean.createProjectJoinRequestNotificationsForProjectAdmins(projectMembershipEntity);
        } else {
            throw new EntityNotFoundException("Project or User not found");
        }
    }

    public void sendJoinRequisitionToManagers(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        List<UserEntity> projectManagers = projectMemberDao.findProjectManagers(projectEntity.getId());
        for (UserEntity manager : projectManagers) {
            emailService.sendJoinRequisitionToManagersEmail(manager.getEmail(), manager.getUsername(), user.getUsername(), projectEntity.getName(), membershipEntity.getAcceptanceToken());
        }
    }

    public void confirmAskToJoinProjectInvite(String token, boolean approve, String approverUsername) throws EntityNotFoundException, UserNotFoundException, UnauthorizedAccessException {
        ProjectMembershipEntity membershipEntity = projectMemberDao.findProjectMembershipByAcceptanceToken(token);
        if (membershipEntity == null) {
            throw new EntityNotFoundException("Project membership not found");
        }
        UserEntity approverEntity = userDao.findUserByUsername(approverUsername);
        if (approverEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!projectMemberDao.isUserProjectManager(membershipEntity.getProject().getId(), approverEntity.getId())) {
            throw new UnauthorizedAccessException("Approver is not a Project Manager");
        }
        if (approve) {
            membershipEntity.setAccepted(true);
            membershipEntity.setAcceptanceToken(null);
            String content = "User " + membershipEntity.getUser().getUsername() + ", added to project by " + approverUsername;
            notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(membershipEntity, true);
            projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
        } else {
            String content = "Application of user " + membershipEntity.getUser().getUsername() + ", to project, rejected by " + approverUsername;
            notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(membershipEntity, false);
            projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
            projectMemberDao.remove(membershipEntity);
        }
    }

    public void sendInviteToUser(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        emailService.sendInvitationToProjectEmail(user.getEmail(), membershipEntity.getAcceptanceToken(), projectEntity.getName());
        notificationBean.createNotificationForProjectInviteFromAProjectManagerToUser(membershipEntity);
    }

    @Transactional
    public void addUserToProject(String username, long projectId, boolean createHasAccepted, boolean isTheCreator, UserEntity authUser) throws EntityNotFoundException, UserNotFoundException, InputValidationException {
        // Find the project by Id
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find user by username
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        // Check if the current number of project members has reached the maximum limit
        int maxProjectElements = configurationBean.getConfigValueByKey("maxProjectMembers");
        if (projectEntity.getMembers().size() >= maxProjectElements) {
            throw new IllegalStateException("Project member's limit is reached");
        }
        // Check if the user is already a member of the project
        if (projectDao.isProjectMember(projectId, userEntity.getId())) {
            throw new IllegalStateException("User is already a member of the project");
        }
        // Create a new ProjectMembershipEntity with the default role NORMAL_USER
        ProjectMembershipEntity membershipEntity = new ProjectMembershipEntity();
        membershipEntity.setUser(userEntity);
        membershipEntity.setProject(projectEntity);
        if (isTheCreator) {
            membershipEntity.setRole(ProjectRoleEnum.PROJECT_MANAGER);
        } else {
            membershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
        }
        membershipEntity.setAccepted(createHasAccepted);
        if (!createHasAccepted) {
            String acceptanceToken = sessionBean.generateNewToken();
            membershipEntity.setAcceptanceToken(acceptanceToken);
        }
        projectMemberDao.persist(membershipEntity);
        // Add the membership to the user's projects
        userEntity.getProjects().add(membershipEntity);
        // Add the user to the project's users
        projectEntity.getMembers().add(membershipEntity);
        if (!createHasAccepted) sendInviteToUser(membershipEntity, userEntity, projectEntity);
        else notificationBean.createNotificationForUserAutomaticallyAddedToProject(membershipEntity);
        if (!userEntity.getUsername().equals(authUser.getUsername())) {
            String content = "User " + userEntity.getUsername() + " added to project";
            projectBean.createProjectLog(projectEntity, authUser, LogTypeEnum.PROJECT_MEMBERS, content);
        }
    }

    public void acceptProjectInvite(String token, boolean approve) throws EntityNotFoundException {
        ProjectMembershipEntity membershipEntity = projectMemberDao.findProjectMembershipByAcceptanceToken(token);
        if (membershipEntity == null) {
            throw new EntityNotFoundException("Project membership not found");
        }
        if (approve) {
            membershipEntity.setAccepted(true);
            membershipEntity.setAcceptanceToken(null);
            String content = "User " + membershipEntity.getUser().getUsername() + ", accepted become project member";
            notificationBean.createNotificationForProjectManagersKnowUserApproval(membershipEntity, true);
            projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
        } else {
            String content = "User " + membershipEntity.getUser().getUsername() + ", refused become project member";
            notificationBean.createNotificationForProjectManagersKnowUserApproval(membershipEntity, false);
            projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
            projectMemberDao.remove(membershipEntity);
        }
    }

    @Transactional
    public void removeUserFromProject(String username, long projectId, SecurityContext securityContext) throws EntityNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        // Find the project by Id
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find user by username
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        if (projectEntity.getCreatedBy().equals(userEntity)) {
            throw new IllegalStateException("User is the creator of the project");
        }
        ProjectMembershipEntity userMembership = null;
        // Check if the user is a member of the project
        for (ProjectMembershipEntity membership : projectEntity.getMembers()) {
            if (membership.getUser().equals(userEntity)) {
                userMembership = membership;
                break;
            }
        }
        // Remove the membership from the user's projects
        if (userMembership != null) {
            notificationBean.createNotificationForUserRemovedFromProject(userMembership);
            projectMemberDao.remove(userMembership);
        } else {
            throw new IllegalStateException("Project does not have the specified user");
        }
        String content = "User " + userEntity.getUsername() + ", was removed from project " + projectEntity.getName() + ", by " + authUserEntity.getUsername();
        projectBean.createProjectLog(projectEntity, userEntity, LogTypeEnum.PROJECT_MEMBERS, content);
    }

    public List<Long> getProjectIdsByUserId(SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        return projectMemberDao.findProjectIdsByUserId(authUserDto.getUserId());
    }

    public List<UserBasicInfoDto> getUsersBasicInfoByFirstLetter(String firstLetter, long projectId) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<UserEntity> users = projectMemberDao.findUsersByFirstLetterAndProjId(lowerCaseFirstLetter, projectId);
        List<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (UserEntity user : users) {
            userBasicInfoDtos.add(new UserBasicInfoDto(user.getId(), user.getUsername(), user.getPhoto(), user.getRole().getId()));
        }
        return userBasicInfoDtos;
    }

    public List<UserEntity> getProjectMembersByProjId(long projectId) {
        return projectMemberDao.findProjectMembersByProjectId(projectId);
    }
}
