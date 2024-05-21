package aor.fpbackend.service;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.exception.*;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletResponse;
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
    public void registerUser (@Valid UserDto user) throws InvalidCredentialsException, UnknownHostException {userBean.register(user);}

    @PUT
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public void confirmRegistration(@QueryParam("token") String token) throws UserConfirmationException {
        userBean.confirmUser(token);
    }

    @PUT
    @Path("/request/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestPasswordReset(RequestResetPasswordDto requestResetPasswordDto) throws InvalidPasswordRequestException {
        userBean.requestPasswordReset(requestResetPasswordDto);
    }
    @PUT
    @Path("/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetPassword(ResetPasswordDto resetPasswordDto) throws InvalidPasswordRequestException {
        userBean.resetPassword(resetPasswordDto);
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
    public Response login(LoginDto userLogin, @Context HttpServletResponse response) throws InvalidCredentialsException {
        return userBean.login(userLogin, response);
    }

    //TODO: De forma a experimentar o security context
    @GET
    @Path("/basic/info")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBasicInfoDto getBasicInfo(@Context SecurityContext securityContext)  {
        UserDto user = (UserDto) securityContext.getUserPrincipal();
        return userBean.getUserBasicInfo(user);
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
    public ProfileDto userInfo(@PathParam("usernameProfile") String usernameProfile, @Context SecurityContext securityContext) throws UserNotFoundException, UnauthorizedAccessException {
        return userBean.getProfileDto(usernameProfile, securityContext);
    }

    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     */
    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public void logout(@HeaderParam("token") String token)throws InvalidCredentialsException {
        userBean.logout(token);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> getAllUsers() {
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
    public void editUserData(@Valid UpdateUserDto updatedUser, @Context SecurityContext securityContext) throws UserNotFoundException, UnknownHostException {
        userBean.updateUserProfile(securityContext, updatedUser);
    }

    @PUT
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateUserPassword(@Valid UpdatePasswordDto updatedPassword, @Context SecurityContext securityContext) throws InvalidPasswordRequestException, UnknownHostException {
        userBean.updatePassword(updatedPassword, securityContext);
    }

    @PUT
    @Path("/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateUserPassword(@Valid UpdateRoleDto updatedRole, @Context SecurityContext securityContext) throws InvalidCredentialsException, UnknownHostException {
        userBean.updateRole(updatedRole, securityContext);
    }

}
