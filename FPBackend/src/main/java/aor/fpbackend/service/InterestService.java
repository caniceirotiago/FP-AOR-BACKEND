package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.dto.InterestAddDto;
import aor.fpbackend.dto.InterestGetDto;
import aor.fpbackend.dto.InterestRemoveDto;
import aor.fpbackend.exception.AttributeAlreadyExistsException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import aor.fpbackend.enums.MethodEnum;

@Path("/interests")
public class InterestService {

    @EJB
    InterestBean interestBean;

    @POST
    @Path("/add/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_INTEREST)
    public void addInterest(@Valid InterestAddDto interestAddDto, @Context SecurityContext securityContext) throws AttributeAlreadyExistsException {
        interestBean.addInterest(interestAddDto, securityContext);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_INTERESTS)
    public List<InterestGetDto> getAllInterests() {
        return interestBean.getInterests();
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.INTEREST_BY_USER)
    public List<InterestGetDto> getAllInterestsByUser(@PathParam("username") String username) {
        return interestBean.getInterestsByUser(username);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.INTEREST_FIRST_LETTER)
    public List<InterestGetDto> getAllInterestsByFirstLetter(@QueryParam("value") String firstLetter) {
        return interestBean.getInterestsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_INTEREST)
    public void removeInterest(@Valid InterestRemoveDto interestRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        interestBean.removeInterest(interestRemoveDto, securityContext);
    }
}
