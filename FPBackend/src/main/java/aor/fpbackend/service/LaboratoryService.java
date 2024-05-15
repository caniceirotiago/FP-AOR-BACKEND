package aor.fpbackend.service;

import aor.fpbackend.bean.LaboratoryBean;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

    @Path("/labs")
    public class LaboratoryService {
        @EJB
        LaboratoryBean labBean;

        @POST
        @Path("/create")
        @Consumes(MediaType.APPLICATION_JSON)
        public void registerUser(String location) {
            labBean.createLaboratory(location);
        }


}
