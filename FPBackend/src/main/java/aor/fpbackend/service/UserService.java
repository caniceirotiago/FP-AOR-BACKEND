package aor.fpbackend.service;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.LoginDto;
import aor.fpbackend.dto.TokenDto;
import aor.fpbackend.dto.UserDto;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;


@Path("/users")
    public class UserService {
        @EJB
        UserBean userBean;



    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addUser(@Valid UserDto user){

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
    public TokenDto login(LoginDto userLogin) {
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
     *  */
    @POST
    @Path("/logout")
    public void logout(@HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        userBean.logout(token);
    }
}
