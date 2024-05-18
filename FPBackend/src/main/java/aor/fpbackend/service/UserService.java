package aor.fpbackend.service;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.InvalidPasswordRequestException;
import aor.fpbackend.exception.UserConfirmationException;
import aor.fpbackend.exception.UserNotFoundException;
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
    public void requestPasswordReset(ResetPasswordDto resetPasswordDto) throws InvalidPasswordRequestException {
        userBean.requestPasswordReset(resetPasswordDto);
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
    @Path("/userBasicInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBasicInfoDto getBasicInfo(@Context SecurityContext securityContext)  {
        System.out.println(securityContext);
        UserDto user = (UserDto) securityContext.getUserPrincipal();
        System.out.println(user + " -2");
        return userBean.getUserBasicInfo(user);
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
    @Path("profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserDto getUserProfile(@HeaderParam("Authorization") String authHeader) throws UserNotFoundException {
        String token = authHeader.substring(7);
        return userBean.getLoggedUser(token);
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
    public void editUserData(@Valid EditProfileDto updatedUser, @HeaderParam("Authorization") String authHeader) throws UserNotFoundException, UnknownHostException {
        String token = authHeader.substring(7);
        userBean.updateUserProfile(token, updatedUser);}



}
