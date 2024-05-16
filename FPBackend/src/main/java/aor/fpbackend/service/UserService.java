package aor.fpbackend.service;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.LoginDto;
import aor.fpbackend.dto.ResetPasswordDto;
import aor.fpbackend.dto.TokenDto;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.InvalidPasswordRequestException;
import aor.fpbackend.exception.UserConfirmationException;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


@Path("/users")
public class UserService {
    @EJB
    UserBean userBean;


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerUser (UserDto user) throws InvalidCredentialsException, UnknownHostException {
        userBean.register(user);
    }

    @PUT
    @Path("/confirm")
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
    public TokenDto login(LoginDto userLogin) throws InvalidCredentialsException {
        return userBean.login(userLogin);
    }


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> getAllUsers() {
        return userBean.getAllRegUsers();
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
}
