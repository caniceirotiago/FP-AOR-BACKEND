package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.dto.InterestDto;
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
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_INTEREST)
    public void addInterest(@Valid InterestDto interestDto, @Context SecurityContext securityContext) {
        interestBean.addInterest(interestDto, securityContext);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_INTERESTS)
    public List<InterestDto> getAllInterests() {
        return interestBean.getInterests();
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.INTEREST_BY_USER)
    public List<InterestDto> getAllInterestsByUser(@Context SecurityContext securityContext) {
        return interestBean.getInterestsByUser(securityContext);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.INTEREST_FIRST_LETTER)
    public List<InterestDto> getAllInterestsByFirstLetter(@QueryParam("value") String firstLetter) {
        return interestBean.getInterestsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_INTEREST)
    public void removeInterest(@Valid InterestDto interestDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        interestBean.removeInterest(interestDto, securityContext);
    }
}
