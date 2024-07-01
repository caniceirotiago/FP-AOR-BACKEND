package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Project.ProjectNameIdDto;
import aor.fpbackend.dto.User.UserBasicInfoDto;
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
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.*;

@Stateless
public class MembershipBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MembershipBean.class);

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


    /**
     * Handles the request of a user to join a project.
     *
     * @param projectId the ID of the project to join
     * @param securityContext the security context containing the authenticated user
     * @throws EntityNotFoundException if the user or project is not found
     * @throws DuplicatedAttributeException if the user is already a member of the project
     * @throws UnknownHostException if an unknown host exception occurs
     */
    public void askToJoinProject(long projectId, SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
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
        try {
            ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
            projectMembershipEntity.setProject(projectEntity);
            projectMembershipEntity.setUser(userEntity);
            projectMembershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
            projectMembershipEntity.setAccepted(false);
            projectMembershipEntity.setAcceptanceToken(UUID.randomUUID().toString());
            projectMemberDao.persist(projectMembershipEntity);
            sendJoinRequisitionToManagers(projectMembershipEntity, userEntity, projectEntity);
            notificationBean.createProjectJoinRequestNotificationsForProjectAdmins(projectMembershipEntity);
            LOGGER.info("User " + userEntity.getUsername() + " asked to join project " + projectEntity.getName());
        } catch (Exception e) {
            LOGGER.error("Error while asking to join project", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }

    /**
     * Sends an email requisition to all project managers for a user's request to join a project.
     *
     * @param membershipEntity the project membership entity containing the join request details
     * @param user the user requesting to join the project
     * @param projectEntity the project to which the user is requesting to join
     */
    public void sendJoinRequisitionToManagers(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        List<UserEntity> projectManagers = projectMemberDao.findProjectManagers(projectEntity.getId());
        for (UserEntity manager : projectManagers) {
            emailService.sendJoinRequisitionToManagersEmail(manager.getEmail(), manager.getUsername(), user.getUsername(), projectEntity.getName(), membershipEntity.getAcceptanceToken());
        }
    }


    /**
     * Confirms or rejects a user's request to join a project based on a provided token.
     *
     * @param token the token associated with the join request
     * @param approve true if the request is approved, false if rejected
     * @param approverUsername the username of the approver
     * @throws EntityNotFoundException if the project membership or approver user is not found
     * @throws UserNotFoundException if the approver user is not found
     * @throws UnauthorizedAccessException if the approver is not a project manager
     * @throws UnknownHostException if an error occurs during notification creation
     */
    public void confirmAskToJoinProjectInvite(String token, boolean approve, String approverUsername) throws EntityNotFoundException, UserNotFoundException, UnauthorizedAccessException, UnknownHostException {
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
        try{
            if (approve) {
                membershipEntity.setAccepted(true);
                membershipEntity.setAcceptanceToken(null);
                String content = "User " + membershipEntity.getUser().getUsername() + ", added to project by " + approverUsername;
                notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(membershipEntity, true);
                projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
                LOGGER.info("User " + membershipEntity.getUser().getUsername() + " accepted to join project " + membershipEntity.getProject().getName());
            } else {
                String content = "Application of user " + membershipEntity.getUser().getUsername() + ", to project, rejected by " + approverUsername;
                notificationBean.createNotificationForProjectJoinRequestApprovedOrRejected(membershipEntity, false);
                projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
                projectMemberDao.remove(membershipEntity);
                LOGGER.info("User " + membershipEntity.getUser().getUsername() + " rejected to join project " + membershipEntity.getProject().getName());
            }
        } catch (Exception e) {
            LOGGER.error("Error while confirming ask to join project invite", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }

    }

    /**
     * Sends an invitation to a user to join a project and creates a notification for the invite.
     *
     * @param membershipEntity the project membership entity containing invitation details
     * @param user the user to be invited
     * @param projectEntity the project to which the user is invited
     * @throws UnknownHostException if an error occurs during notification creation
     */
    public void sendInviteToUser(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) throws UnknownHostException {
        try {
            emailService.sendInvitationToProjectEmail(user.getEmail(), membershipEntity.getAcceptanceToken(), projectEntity.getName());
            LOGGER.info("Sent project invitation email to user {}", user.getUsername());
            notificationBean.createNotificationForProjectInviteFromAProjectManagerToUser(membershipEntity);
            LOGGER.info("Created notification for project invite to user {}", user.getUsername());
        } catch (Exception e) {
            LOGGER.error("Error sending invite to user: {}", e.getMessage());
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }


    /**
     * Adds a user to a project, either directly or via an invite, and creates the necessary notifications and logs.
     *
     * @param username the username of the user to be added
     * @param projectId the ID of the project to which the user is being added
     * @param createHasAccepted flag indicating if the user has already accepted the invite
     * @param isTheCreator flag indicating if the user is the creator of the project
     * @param authUser the authenticated user performing the action
     * @throws EntityNotFoundException if the project or user is not found
     * @throws UserNotFoundException if the user is not found
     * @throws InputValidationException if there is a validation error
     * @throws UnknownHostException if an error occurs during notification creation
     */
    @Transactional
    public void addUserToProject(String username, long projectId, boolean createHasAccepted, boolean isTheCreator, UserEntity authUser) throws EntityNotFoundException, UserNotFoundException, InputValidationException, UnknownHostException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        int maxProjectElements = configurationBean.getConfigValueByKey("maxProjectMembers");
        if (projectEntity.getMembers().size() >= maxProjectElements) {
            throw new IllegalStateException("Project member's limit is reached");
        }
        if (projectDao.isProjectMember(projectId, userEntity.getId())) {
            throw new IllegalStateException("User is already a member of the project");
        }
        try{
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
            userEntity.getProjects().add(membershipEntity);
            projectEntity.getMembers().add(membershipEntity);
            if (!createHasAccepted) sendInviteToUser(membershipEntity, userEntity, projectEntity);
            else notificationBean.createNotificationForUserAutomaticallyAddedToProject(membershipEntity);
            if (!userEntity.getUsername().equals(authUser.getUsername())) {
                String content = "User " + userEntity.getUsername() + " added to project";
                projectBean.createProjectLog(projectEntity, authUser, LogTypeEnum.PROJECT_MEMBERS, content);
            }
            LOGGER.info("User " + userEntity.getUsername() + " added to project " + projectEntity.getName());
        } catch (Exception e) {
            LOGGER.error("Error while adding user to project", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }


    /**
     * Handles the acceptance or rejection of a project invite based on the provided token.
     *
     * @param token the acceptance token associated with the project invite
     * @param approve flag indicating whether the invite is approved or not
     * @throws EntityNotFoundException if the project membership is not found
     * @throws UnknownHostException if an error occurs during notification creation
     */
    public void acceptProjectInvite(String token, boolean approve) throws EntityNotFoundException, UnknownHostException {
       if (token == null) {
            throw new EntityNotFoundException("Project membership not found");
       }
       ProjectMembershipEntity membershipEntity = projectMemberDao.findProjectMembershipByAcceptanceToken(token);
       if (membershipEntity == null) {
           throw new EntityNotFoundException("Project membership not found");
       }
       try{
           if (approve) {
                membershipEntity.setAccepted(true);
                membershipEntity.setAcceptanceToken(null);
                String content = "User " + membershipEntity.getUser().getUsername() + ", accepted become project member";
                notificationBean.createNotificationForProjectManagersKnowUserApproval(membershipEntity, true);
                projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
                LOGGER.info("User " + membershipEntity.getUser().getUsername() + " accepted to become project member");
           } else {
               String content = "User " + membershipEntity.getUser().getUsername() + ", refused become project member";
               notificationBean.createNotificationForProjectManagersKnowUserApproval(membershipEntity, false);
               projectBean.createProjectLog(membershipEntity.getProject(), membershipEntity.getUser(), LogTypeEnum.PROJECT_MEMBERS, content);
               projectMemberDao.remove(membershipEntity);
               LOGGER.info("User " + membershipEntity.getUser().getUsername() + " refused to become project member");
           }
       } catch (Exception e) {
           LOGGER.error("Error while accepting project invite", e);
           throw e;
       } finally {
           ThreadContext.clearAll();
       }
    }


    /**
     * Removes a user from a project and creates appropriate notifications and logs.
     *
     * @param username the username of the user to be removed
     * @param projectId the ID of the project from which the user is to be removed
     * @param securityContext the security context containing the authenticated user's information
     * @throws EntityNotFoundException if the project or user is not found
     * @throws UnknownHostException if an error occurs during notification creation
     */
    @Transactional
    public void removeUserFromProject(String username, long projectId, SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
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

    /**
     * Retrieves a list of projects that the authenticated user is a member of.
     *
     * @param userId the ID of the user
     * @return a list of ProjectNameIdDto containing the IDs and names of the projects
     */
    public List<ProjectNameIdDto> getProjectIdsByUserId(Long userId) {
        List<Long> projectsIds = projectMemberDao.findProjectIdsByUserId(userId);
        List<ProjectNameIdDto> projectNameIdDtos = new ArrayList<>();
        for (Long projectId : projectsIds) {
            ProjectEntity project = projectDao.findProjectById(projectId);
            projectNameIdDtos.add(new ProjectNameIdDto(project.getId(), project.getName(), project.getCreationDate(), project.getState()));
        }
        return projectNameIdDtos;
    }


    /**
     * Retrieves a list of users' basic information whose usernames start with a specified first letter and are members of a specified project.
     *
     * @param firstLetter the first letter to filter the usernames
     * @param projectId the ID of the project
     * @return a list of UserBasicInfoDto containing the basic information of users
     */
    public List<UserBasicInfoDto> getUsersBasicInfoByFirstLetter(String firstLetter, long projectId) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
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


    /**
     * Retrieves a list of user entities who are members of a specified project.
     *
     * @param projectId the ID of the project
     * @return a list of UserEntity objects representing the members of the project
     */
    public List<UserEntity> getProjectMembersByProjId(long projectId) {
        return projectMemberDao.findProjectMembersByProjectId(projectId);
    }
}
