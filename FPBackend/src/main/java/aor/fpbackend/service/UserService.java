package aor.fpbackend.service;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.net.UnknownHostException;
import java.util.List;


@Path("/users")
public class UserService {
    @EJB
    UserBean userBean;

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerUser(@Valid UserRegisterDto user) throws InvalidCredentialsException, UnknownHostException {
        userBean.register(user);
    }

    @PUT
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public void confirmRegistration(@QueryParam("token") String token) throws UserConfirmationException {
        userBean.confirmUser(token);
    }
    @POST
    @Path("/request/confirmation/email")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
        userBean.requestNewConfirmationEmail(email);
    }

    @PUT
    @Path("/request/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestPasswordReset(@Valid PasswordRequestResetDto passwordRequestResetDto) throws InvalidPasswordRequestException {
        userBean.requestPasswordReset(passwordRequestResetDto);
    }

    @PUT
    @Path("/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetPassword(@Valid PasswordResetDto passwordResetDto) throws InvalidPasswordRequestException {
        userBean.resetPassword(passwordResetDto);
    }

    /**
     * This endpoint is responsible for user authentication. It accepts JSON-formatted requests containing
     * user credentials (username and password) as headers. It returns appropriate responses indicating the
     * success or failure of the login attempt.
     * Successful login returns a status code of 200, failed login returns 401, and missing username or password
     * returns 422.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid UserLoginDto userLogin) throws InvalidCredentialsException {
        return userBean.login(userLogin);
    }

    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     */
    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public void logout(@Context SecurityContext securityContext) throws InvalidCredentialsException, UnknownHostException {
        userBean.logout(securityContext);
    }

    @GET
    @Path("/basic/info")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBasicInfoDto getBasicInfo(@Context SecurityContext securityContext) {
        return userBean.getUserBasicInfo(securityContext);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.USERS_FIRST_LETTER)
    public List<UserBasicInfoDto> getBasicInfoByFirstLetter(@QueryParam("value") String firstLetter) {
        return userBean.getUsersBasicInfoByFirstLetter(firstLetter);
    }

    /**
     * Retrieves user information for the given username.
     * If the username or password is missing in the request headers, returns a status code 401 (Unauthorized)
     * with the error message "User not logged in".
     * If the provided credentials are invalid, returns a status code 403 (Forbidden) with the error message "Access denied".
     * If the user information is successfully retrieved, returns a status code 200 (OK) with the user information
     * (without the password) in JSON format.
     */
    @GET
    @Path("info/{usernameProfile}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserProfileDto userInfo(@PathParam("usernameProfile") String usernameProfile, @Context SecurityContext securityContext) throws UserNotFoundException, UnauthorizedAccessException, ForbiddenAccessException {
        return userBean.getProfileDto(usernameProfile, securityContext);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UsernameDto> getAllUsers() {
        return userBean.getAllRegUsers();
    }

    /**
     * Allows an authenticated user to update their own data. It checks for valid authentication and
     * proper permissions before allowing the update. The method ensures that the user can only update
     * their own information and not that of others unless specifically authorized.
     */
    @PUT
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public void editUserData(@Valid UserUpdateDto updatedUser, @Context SecurityContext securityContext) throws UserNotFoundException, UnknownHostException {
        userBean.updateUserProfile(securityContext, updatedUser);
    }

    @PUT
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateUserPassword(@Valid PasswordUpdateDto updatedPassword, @Context SecurityContext securityContext) throws InvalidPasswordRequestException, UnknownHostException {
        userBean.updatePassword(updatedPassword, securityContext);
    }

    @PUT
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.UPDATE_ROLE)
    public void updateUserRole(@Valid UserUpdateRoleDto updatedRole) throws InvalidCredentialsException, UnknownHostException {
        userBean.updateRole(updatedRole);
    }

    @GET
    @Path("/session/check")
    @Produces(MediaType.APPLICATION_JSON)
    public void checkSession() {}

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProjectMembershipDto> getUsersByProject(@PathParam("projectId") long projectId) {
        return userBean.getUsersByProject(projectId);
    }
}
