package aor.fpbackend.service;

import aor.fpbackend.bean.LaboratoryBean;
import aor.fpbackend.dto.LaboratoryDto;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/labs")
public class LaboratoryService {
    @EJB
    LaboratoryBean labBean;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerUser(String location) throws DatabaseOperationException {
        labBean.createLaboratoryIfNotExists(location);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<LaboratoryDto> getAllLabs() {
        return labBean.getLaboratories();
    }


}
