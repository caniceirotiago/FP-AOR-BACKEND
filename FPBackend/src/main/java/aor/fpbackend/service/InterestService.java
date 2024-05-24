package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.dto.LaboratoryDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/interests")
public class InterestService {

    @EJB
    InterestBean interestBean;
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createInterest(InterestDto interestDto) {
        interestBean.createInterest(interestDto.getName());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InterestDto> getAllInterests() {
          return interestBean.getInterests();
    }
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    public List<InterestDto> getAllInterestsByFirstLetter(@QueryParam("value") String firstLetter) {
        return interestBean.getInterestsByFirstLetter(firstLetter);
    }



}
