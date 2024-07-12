package aor.fpbackend.service;

import aor.fpbackend.bean.LaboratoryBean;
import aor.fpbackend.dto.Laboratory.LaboratoryDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
/**
 * LaboratoryService is a JAX-RS resource class that provides RESTful endpoints for managing laboratories,
 * including retrieving a list of all laboratories.
 */
@Path("/labs")
public class LaboratoryService {
    @EJB
    LaboratoryBean labBean;
    /**
     * Retrieves a list of all laboratories.
     *
     * @return an ArrayList of LaboratoryDto representing all laboratories.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<LaboratoryDto> getAllLabs() {
        return labBean.getLaboratories();
    }


}
