package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/assets")
public class AssetService {
    @EJB
    AssetBean assetBean;


    @POST
    @Path("/add/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_ASSET)
    public void createAsset(@PathParam("projectId") long projectId, @Valid AssetAddDto assetAddDto) throws EntityNotFoundException, InputValidationException {
        if (assetAddDto != null) {
            assetBean.addAsset(assetAddDto.getName(), assetAddDto.getType(), assetAddDto.getDescription(), assetAddDto.getStockQuantity(),
                    assetAddDto.getPartNumber(), assetAddDto.getManufacturer(), assetAddDto.getManufacturerPhone(),
                    assetAddDto.getObservations(), projectId, assetAddDto.getUsedQuantity());
        } else {
            throw new InputValidationException("Invalid Dto");
        }
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_ASSETS)
    public List<AssetGetDto> getAllAssets() {
        return assetBean.getAllAssets();
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_BY_PROJECT)
    public List<AssetGetDto> getAssetsByProject(@PathParam("projectId") long projectId) {
        return assetBean.getAssetsByProject(projectId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_FIRST_LETTER)
    public List<AssetGetDto> getAssetsFirstLetter(@QueryParam("value") String firstLetter) {
        return assetBean.getAssetsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_ASSET)
    public void removeAsset(@Valid AssetRemoveDto assetRemoveDto) throws EntityNotFoundException {
        assetBean.removeAsset(assetRemoveDto);
    }
}