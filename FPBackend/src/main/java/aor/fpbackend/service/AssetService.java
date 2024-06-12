package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;


@Path("/assets")
public class AssetService {
    @EJB
    AssetBean assetBean;


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public void createAsset(@Valid AssetCreateDto assetCreateDto) throws InputValidationException, DuplicatedAttributeException {
        assetBean.createAsset(assetCreateDto);
    }

    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void addAssetToProject(@Valid AssetAddDto assetAddDto) throws EntityNotFoundException, InputValidationException {
        if (assetAddDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        assetBean.addAssetToProject(assetAddDto.getName(), assetAddDto.getProjectId(), assetAddDto.getUsedQuantity());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public List<AssetGetDto> getAllAssets() {
        return assetBean.getAllAssets();
    }

    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public AssetsPaginatedDto getFilteredAssets(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Context UriInfo uriInfo) {
        return assetBean.getFilteredAssets(page, pageSize, uriInfo);
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<AssetGetDto> getAssetsByProject(@PathParam("projectId") long projectId) {
        return assetBean.getAssetsByProject(projectId);
    }

    @GET
    @Path("/id/{assetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public AssetGetDto getAssetById(@PathParam("assetId") long assetId) {
        return assetBean.getAssetById(assetId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public List<AssetGetDto> getAssetsFirstLetter(@QueryParam("value") String firstLetter) {
        return assetBean.getAssetsByFirstLetter(firstLetter);
    }

    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public List<AssetTypeEnum> getAssetTypes() {
        return assetBean.getEnumListAssetTypes();
    }

    @PUT
    @Path("/remove/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeAsset(@Valid AssetRemoveDto assetRemoveDto) throws EntityNotFoundException {
        assetBean.removeAsset(assetRemoveDto);
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.STANDARD_LEVEL_ASSETS)
    public void updateAsset(@Valid AssetUpdateDto assetUpdateDto) throws EntityNotFoundException, InputValidationException {
        assetBean.updateAsset(assetUpdateDto);
    }
}