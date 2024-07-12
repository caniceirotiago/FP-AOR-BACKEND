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
/**
 * ConfigurationService is a JAX-RS resource class that provides RESTful endpoints for managing configurations.
 */
@Path("/configurations")
public class ConfigurationService {
    @EJB
    ConfigurationBean configurationBean;
    /**
     * Retrieves all configurations.
     *
     * @return a list of ConfigurationGetDto representing all configurations.
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConfigurationGetDto> getAllConfiguration() {
        return configurationBean.getAllConfiguration();
    }

    /**
     * Updates a configuration value identified by its key.
     *
     * @param configUpdateDto the ConfigurationUpdateDto containing the key and the new value.
     * @throws InputValidationException if the input data is invalid.
     * @throws EntityNotFoundException if the configuration with the given key is not found.
     */
    @PUT
    @Path("/config/key")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.UPDATE_CONFIG)
    public void updateSessionTimeout(@Valid ConfigurationUpdateDto configUpdateDto) throws InputValidationException, EntityNotFoundException {
        configurationBean.updateConfigValue(configUpdateDto);
    }

}
