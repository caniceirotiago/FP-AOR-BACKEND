package aor.fpbackend.service;

import aor.fpbackend.bean.ConfigurationBean;
import aor.fpbackend.dto.Configuration.ConfigurationGetDto;
import aor.fpbackend.dto.Configuration.ConfigurationUpdateDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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

    @PUT
    @Path("/config/key")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.UPDATE_CONFIG)
    public void updateSessionTimeout(@Valid ConfigurationUpdateDto configUpdateDto) throws InputValidationException, EntityNotFoundException {
        configurationBean.updateConfigValue(configUpdateDto);
    }

}
