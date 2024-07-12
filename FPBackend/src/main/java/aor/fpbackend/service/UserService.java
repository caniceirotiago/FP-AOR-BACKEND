package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.bean.SessionBean;
import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.Email.EmailDto;
import aor.fpbackend.dto.Password.PasswordRequestResetDto;
import aor.fpbackend.dto.Password.PasswordResetDto;
import aor.fpbackend.dto.Password.PasswordUpdateDto;
import aor.fpbackend.dto.Project.ProjectMembershipDto;
import aor.fpbackend.dto.User.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.net.UnknownHostException;
import java.util.List;

/**
 * UserService is a JAX-RS resource class that provides RESTful endpoints for user management,
 * including registration, authentication, profile updates, and role management.
 */
@Path("/users")
public class UserService {
    @EJB
    UserBean userBean;
    @EJB
    SessionBean sessionBean;
    @EJB
    MembershipBean memberBean;

    /**
     * Registers a new user.
     *
     * @param user the user registration data.
     * @throws InvalidCredentialsException if the provided credentials are invalid.
     * @throws EntityNotFoundException if required entities are not found.
     */
    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerUser(@Valid UserRegisterDto user) throws InvalidCredentialsException, EntityNotFoundException {
        userBean.register(user);
    }

    /**
     * Confirms user registration.
     *
     * @param token the confirmation token.
     * @throws InputValidationException if the input validation fails.
     * @throws UserNotFoundException if the user is not found.
     */
    @PUT
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public void confirmRegistration(@QueryParam("token") String token) throws InputValidationException, UserNotFoundException {
        userBean.confirmUser(token);
    }

    /**
     * Requests a new confirmation email.
     *
     * @param email the email data transfer object.
     * @throws InvalidRequestOnRegistConfirmationException if the request is invalid.
     */
    @POST
    @Path("/request/confirmation/email")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
        userBean.requestNewConfirmationEmail(email);
    }

    /**
     * Requests a password reset.
     *
     * @param passwordRequestResetDto the password reset request data transfer object.
     * @throws UserNotFoundException if the user is not found.
     * @throws ForbiddenAccessException if the request is forbidden.
     */
    @PUT
    @Path("/request/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestPasswordReset(@Valid PasswordRequestResetDto passwordRequestResetDto) throws UserNotFoundException, ForbiddenAccessException {
        userBean.requestPasswordReset(passwordRequestResetDto);
    }

    /**
     * Resets the user's password.
     *
     * @param passwordResetDto the password reset data transfer object.
     * @throws UserNotFoundException if the user is not found.
     * @throws ForbiddenAccessException if the request is forbidden.
     */
    @PUT
    @Path("/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetPassword(@Valid PasswordResetDto passwordResetDto) throws UserNotFoundException, ForbiddenAccessException {
        userBean.resetPassword(passwordResetDto);
    }

    /**
     * Authenticates a user.
     *
     * @param userLogin the user login data transfer object.
     * @return a response indicating the outcome of the login attempt.
     * @throws InvalidCredentialsException if the provided credentials are invalid.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid UserLoginDto userLogin) throws InvalidCredentialsException {
        return sessionBean.login(userLogin);
    }

    /**
     * Logs out the authenticated user.
     *
     * @param securityContext the security context.
     * @throws UserNotFoundException if the user is not found.
     */
    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public void logout(@Context SecurityContext securityContext) throws UserNotFoundException {
        sessionBean.logout(securityContext);
    }

    /**
     * Retrieves basic information about the authenticated user.
     *
     * @param securityContext the security context.
     * @return the basic user information.
     * @throws UserNotFoundException if the user is not found.
     */
    @GET
    @Path("/basic/info")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBasicInfoDto getBasicInfo(@Context SecurityContext securityContext) throws UserNotFoundException {
        return userBean.getUserBasicInfo(securityContext);
    }

    /**
     * Retrieves basic information about all users.
     *
     * @param securityContext the security context.
     * @return a list of basic user information.
     * @throws UserNotFoundException if the user is not found.
     */
    @GET
    @Path("/all/basic/info")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.USERS_BASIC_INFO)
    public List<UserBasicInfoDto> getUsersListBasicInfo(@Context SecurityContext securityContext) throws UserNotFoundException {
        return userBean.getUsersListBasicInfo(securityContext);
    }

    /**
     * Retrieves basic information about a user by username.
     *
     * @param username the username.
     * @return the basic user information.
     * @throws UserNotFoundException if the user is not found.
     */
    @GET
    @Path("/basic/info/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBasicInfoDto getUserBasicInfo(@PathParam("username") String username) throws UserNotFoundException {
        return userBean.getUserBasicInfo(username);
    }

    /**
     * Retrieves basic information about users whose usernames start with a specific letter.
     *
     * @param firstLetter the first letter.
     * @return a list of basic user information.
     */
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.USERS_FIRST_LETTER)
    public List<UserBasicInfoDto> getBasicInfoByFirstLetter(@QueryParam("value") String firstLetter) {
        return userBean.getUsersBasicInfoByFirstLetter(firstLetter);
    }

    /**
     * Retrieves information for users to be used as email recipients whose usernames start with a specific letter.
     *
     * @param firstLetter the first letter.
     * @return a list of user information.
     */
    @GET
    @Path("/first/letter/email")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.USERS_FIRST_LETTER)
    public List<UserMessageInfoDto> getUserEmailRecipientByFirstLetter(@QueryParam("value") String firstLetter) {
        return userBean.getUserEmailRecipientByFirstLetter(firstLetter);
    }

    /**
     * Retrieves profile information for a user by username.
     *
     * @param usernameProfile the username.
     * @param securityContext the security context.
     * @return the user profile information.
     * @throws UserNotFoundException if the user is not found.
     * @throws ForbiddenAccessException if access is forbidden.
     */
    @GET
    @Path("info/{usernameProfile}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserProfileDto userInfo(@PathParam("usernameProfile") String usernameProfile, @Context SecurityContext securityContext) throws UserNotFoundException, ForbiddenAccessException {
        return userBean.getProfileDto(usernameProfile, securityContext);
    }

    /**
     * Allows an authenticated user to update their own profile data.
     *
     * @param updatedUser the updated user data.
     * @param securityContext the security context.
     * @throws UserNotFoundException if the user is not found.
     * @throws EntityNotFoundException if the entity is not found.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @PUT
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void editUserData(@Valid UserUpdateDto updatedUser, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException, DatabaseOperationException {
        userBean.updateUserProfile(securityContext, updatedUser);
    }

    /**
     * Updates the password of an authenticated user.
     *
     * @param updatedPassword the updated password data.
     * @param securityContext the security context.
     * @throws UserNotFoundException if the user is not found.
     * @throws InputValidationException if the input validation fails.
     */
    @PUT
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateUserPassword(@Valid PasswordUpdateDto updatedPassword, @Context SecurityContext securityContext) throws UserNotFoundException, InputValidationException {
        userBean.updatePassword(updatedPassword, securityContext);
    }

    /**
     * Updates the role of a user.
     *
     * @param updatedRole the updated role data.
     * @throws InvalidCredentialsException if the credentials are invalid.
     * @throws UnknownHostException if the host is unknown.
     * @throws EntityNotFoundException if the entity is not found.
     */
    @PUT
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.UPDATE_ROLE)
    public void updateUserRole(@Valid UserUpdateRoleDto updatedRole) throws InvalidCredentialsException, UnknownHostException, EntityNotFoundException {
        userBean.updateRole(updatedRole);
    }

    /**
     * Checks the user session.
     */
    @GET
    @Path("/session/check")
    @Produces(MediaType.APPLICATION_JSON)
    public void checkSession() {}

    /**
     * Retrieves users by project ID.
     *
     * @param projectId the project ID.
     * @return a list of project membership data transfer objects.
     */
    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectMembershipDto> getUsersByProject(@PathParam("projectId") long projectId) {
        return memberBean.getProjectMembershipsByProject(projectId);
    }

}
