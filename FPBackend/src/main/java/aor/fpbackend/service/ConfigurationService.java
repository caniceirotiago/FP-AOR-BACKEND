package aor.fpbackend.service;

import aor.fpbackend.bean.ConfigurationBean;
import aor.fpbackend.dto.ConfigurationGetDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/configurations")
public class ConfigurationService {
    @EJB
    ConfigurationBean configurationBean;
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConfigurationGetDto> getAllConfiguration() {
        return configurationBean.getAllConfiguration();
    }

}
