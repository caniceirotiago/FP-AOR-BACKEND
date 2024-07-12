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
/**
 * InterestService is a JAX-RS resource class that provides RESTful endpoints for managing interests,
 * including adding, retrieving, and removing interests.
 */
@Path("/interests")
public class InterestService {

    @EJB
    InterestBean interestBean;
    /**
     * Adds an interest to a user.
     *
     * @param interestAddDto the DTO containing the interest details.
     * @param securityContext the security context containing user details.
     * @throws DuplicatedAttributeException if the interest already exists.
     */
    @POST
    @Path("/add/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_INTEREST)
    public void addInterest(@Valid InterestAddDto interestAddDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException {
        interestBean.addInterest(interestAddDto, securityContext);
    }

    /**
     * Retrieves a list of all interests.
     *
     * @return a list of InterestGetDto representing all interests.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_INTERESTS)
    public List<InterestGetDto> getAllInterests() {
        return interestBean.getInterests();
    }

    /**
     * Retrieves a list of interests for a specific user.
     *
     * @param username the username of the user.
     * @return a list of InterestGetDto representing the interests of the specified user.
     */
    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTERESTS_BY_USER)
    public List<InterestGetDto> getAllInterestsByUser(@PathParam("username") String username) {
        return interestBean.getInterestsByUser(username);
    }

    /**
     * Retrieves a list of interests that start with the specified first letter.
     *
     * @param firstLetter the first letter to filter interests by.
     * @return a list of InterestGetDto representing the interests that start with the specified letter.
     */
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTERESTS_FIRST_LETTER)
    public List<InterestGetDto> getAllInterestsByFirstLetter(@QueryParam("value") String firstLetter) {
        return interestBean.getInterestsByFirstLetter(firstLetter);
    }

    /**
     * Retrieves a list of interest types.
     *
     * @return a list of InterestTypeEnum representing the interest types.
     */
    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.INTEREST_ENUMS)
    public List<InterestTypeEnum> getInterestTypes() {
        return interestBean.getEnumListInterestTypes();
    }

    /**
     * Removes an interest from a user.
     *
     * @param interestRemoveDto the DTO containing the interest details to be removed.
     * @param securityContext the security context containing user details.
     * @throws UserNotFoundException if the user is not found.
     * @throws EntityNotFoundException if the interest is not found.
     */
    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_INTEREST)
    public void removeInterest(@Valid InterestRemoveDto interestRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        interestBean.removeInterest(interestRemoveDto, securityContext);
    }

}
