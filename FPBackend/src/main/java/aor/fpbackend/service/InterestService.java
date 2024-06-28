package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.dto.Interest.InterestAddDto;
import aor.fpbackend.dto.Interest.InterestGetDto;
import aor.fpbackend.dto.Interest.InterestRemoveDto;
import aor.fpbackend.enums.InterestTypeEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

import aor.fpbackend.filters.RequiresMethodPermission;
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
    @RequiresMethodPermission(MethodEnum.ADD_INTEREST)
    public void addInterest(@Valid InterestAddDto interestAddDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException, InputValidationException {
        interestBean.addInterest(interestAddDto, securityContext);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_INTERESTS)
    public List<InterestGetDto> getAllInterests() {
        return interestBean.getInterests();
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTERESTS_BY_USER)
    public List<InterestGetDto> getAllInterestsByUser(@PathParam("username") String username) {
        return interestBean.getInterestsByUser(username);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTERESTS_FIRST_LETTER)
    public List<InterestGetDto> getAllInterestsByFirstLetter(@QueryParam("value") String firstLetter) {
        return interestBean.getInterestsByFirstLetter(firstLetter);
    }

    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTEREST_ENUMS)
    public List<InterestTypeEnum> getInterestTypes() {
        return interestBean.getEnumListInterestTypes();
    }

    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_INTEREST)
    public void removeInterest(@Valid InterestRemoveDto interestRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        System.out.println("Removing interest");
        interestBean.removeInterest(interestRemoveDto, securityContext);
    }

}
