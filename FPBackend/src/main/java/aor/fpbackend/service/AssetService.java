package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.filters.RequiresPermission;
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
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_ASSET)
    public void createAsset(@Valid AssetAddDto assetAddDto) throws EntityNotFoundException {
        assetBean.addAsset(assetAddDto);

    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_ASSETS)
    public List<AssetGetDto> getAllAssets() {
        return assetBean.getAllAssets();
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ASSET_BY_PROJECT)
    public List<AssetGetDto> getAssetsByProject(@PathParam("projectId") long projectId) {
        return assetBean.getAssetsByProject(projectId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ASSET_FIRST_LETTER)
    public List<AssetGetDto> getAssetsFirstLetter(@QueryParam("value") String firstLetter) {
        return assetBean.getAssetsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_ASSET)
    public void removeAsset(@Valid AssetRemoveDto assetRemoveDto) throws EntityNotFoundException {
        assetBean.removeAsset(assetRemoveDto);
    }
}