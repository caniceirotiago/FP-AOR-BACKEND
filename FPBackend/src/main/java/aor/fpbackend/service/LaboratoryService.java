package aor.fpbackend.service;

import aor.fpbackend.bean.LaboratoryBean;
import aor.fpbackend.dto.LaboratoryDto;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;

@Path("/labs")
public class LaboratoryService {
    @EJB
    LaboratoryBean labBean;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<LaboratoryDto> getAllLabs() {
        return labBean.getLaboratories();
    }


}
