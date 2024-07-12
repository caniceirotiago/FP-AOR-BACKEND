package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Project.ProjectMembershipDto;
import aor.fpbackend.dto.Project.ProjectNameIdDto;
import aor.fpbackend.dto.User.UserBasicInfoDto;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.net.UnknownHostException;
import java.util.List;

/**
 * MembershipService is a JAX-RS resource class that provides RESTful endpoints for managing project memberships,
 * including adding, removing, and retrieving members.
 */
@Path("/memberships")
public class MembershipService {

    @EJB
    MembershipBean memberBean;
    @EJB
    UserDao userDao;
    /**
     * Allows a user to request to join a project.
     *
     * @param projectId the ID of the project to join.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the project is not found.
     * @throws DuplicatedAttributeException if the user has already requested to join the project.
     * @throws UnknownHostException if there is a network error.
     * @throws ElementAssociationException if there is an issue with the user's association to the project.
     */
    @POST
    @Path("/ask/join/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASK_TO_JOIN)
    public void askToJoinProject(@PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, UnknownHostException, ElementAssociationException {
        memberBean.askToJoinProject(projectId, securityContext);
    }

    /**
     * Confirms a user's request to join a project.
     *
     * @param token the token for the request.
     * @param approve whether to approve the request.
     * @param approverUsername the username of the approver.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws UserNotFoundException if the user is not found.
     * @throws UnauthorizedAccessException if the approver is not authorized.
     * @throws UnknownHostException if there is a network error.
     */
    @PUT
    @Path("/confirm/project")
    @Consumes(MediaType.APPLICATION_JSON)
    public void confirmAskToJoinProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve, @QueryParam("approver") String approverUsername) throws EntityNotFoundException, UserNotFoundException, UnauthorizedAccessException, UnknownHostException {
        memberBean.confirmAskToJoinProjectInvite(token, approve, approverUsername);
    }

    /**
     * Adds a user to a project.
     *
     * @param username the username of the user to add.
     * @param projectId the ID of the project.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws UnknownHostException if there is a network error.
     * @throws ElementAssociationException if there is an issue with the user's association to the project.
     */
    @POST
    @Path("/add/{username}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void addUserToProject(@PathParam("username") String username, @PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException, ElementAssociationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        memberBean.addUserToProject(username, projectId, false, false, authUserEntity);
    }

    /**
     * Accepts or rejects a project invitation.
     *
     * @param token the token for the invitation.
     * @param approve whether to approve the invitation.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws UnknownHostException if there is a network error.
     */
    @PUT
    @Path("/accept/project")
    @Consumes(MediaType.APPLICATION_JSON)
    public void acceptProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve) throws EntityNotFoundException, UnknownHostException {
        memberBean.acceptProjectInvite(token, approve);
    }

    /**
     * Removes a user from a project.
     *
     * @param username the username of the user to remove.
     * @param projectId the ID of the project.
     * @param securityContext the security context.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws UnknownHostException if there is a network error.
     * @throws ForbiddenAccessException if the user does not have permission to remove the user.
     */
    @DELETE
    @Path("/remove/{username}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_USER_PROJECT)
    public void removeUserFromProject(@PathParam("username") String username, @PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException, ForbiddenAccessException {
        System.out.println("Removing user from project");
        memberBean.removeUserFromProject(username, projectId, securityContext);
    }

    /**
     * Retrieves a list of project IDs for a specific user.
     *
     * @param userId the ID of the user.
     * @return a list of ProjectNameIdDto representing the user's projects.
     */
    @GET
    @Path("/projectIds/byUserId/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectNameIdDto> getProjectIdsByUserId(@PathParam("userId") long userId){
        return memberBean.getProjectIdsByUserId(userId);
    }

    /**
     * Retrieves a list of users' basic information by the first letter of their username for a specific project.
     *
     * @param firstLetter the first letter of the username.
     * @param projectId the ID of the project.
     * @return a list of UserBasicInfoDto representing the users.
     */
    @GET
    @Path("/first/letter/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserBasicInfoDto> getBasicInfoByFirstLetter(@QueryParam("value") String firstLetter, @PathParam("projectId") long projectId) {
        return memberBean.getUsersBasicInfoByFirstLetter(firstLetter, projectId);
    }

}
