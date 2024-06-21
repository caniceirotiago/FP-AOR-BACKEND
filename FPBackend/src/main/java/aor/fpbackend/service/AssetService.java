package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.*;
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
    @RequiresMethodPermission(MethodEnum.CREATE_ASSET)
    public void createAsset(@Valid AssetCreateDto assetCreateDto) throws DuplicatedAttributeException {
        assetBean.createAsset(assetCreateDto);
    }

    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_ASSET)
    public void addAssetToProject(@Valid ProjectAssetCreateDto projectAssetCreateDto) throws EntityNotFoundException, ElementAssociationException {
        assetBean.addProjectAssetToProject(projectAssetCreateDto.getName(), projectAssetCreateDto.getProjectId(), projectAssetCreateDto.getUsedQuantity());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_ASSETS)
    public List<AssetGetDto> getAllAssets() {
        return assetBean.getAllAssets();
    }

    @GET
    @Path("/all/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.FILTER_ASSETS)
    public AssetPaginatedDto getFilteredAssets(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("8") int pageSize,
            @Context UriInfo uriInfo) throws InputValidationException {
        return assetBean.getFilteredAssets(page, pageSize, uriInfo);
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_BY_PROJECT)
    public List<ProjectAssetGetDto> getProjectAssetsByProject(@PathParam("projectId") long projectId) {
        return assetBean.getProjectAssetsByProject(projectId);
    }

    @GET
    @Path("/id/{assetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_BY_ID)
    public AssetGetDto getAssetById(@PathParam("assetId") long assetId) {
        return assetBean.getAssetById(assetId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_FIRST_LETTER)
    public List<AssetGetDto> getAssetsFirstLetter(@QueryParam("value") String firstLetter) {
        return assetBean.getAssetsByFirstLetter(firstLetter);
    }

    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSET_ENUMS)
    public List<AssetTypeEnum> getAssetTypes() {
        return assetBean.getEnumListAssetTypes();
    }

    @DELETE
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSET_REMOVE)
    public void removeAsset(@Valid AssetRemoveDto assetRemoveDto) throws EntityNotFoundException, ElementAssociationException {
        assetBean.removeAsset(assetRemoveDto);
    }

    // /{projectId} just for filter validation
    @PUT
    @Path("/remove/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeProjectAssetFromProject(@Valid ProjectAssetRemoveDto projectAssetRemoveDto) throws EntityNotFoundException {
        assetBean.removeProjectAssetFromProject(projectAssetRemoveDto);
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSET_UPDATE)
    public void updateAsset(@Valid AssetUpdateDto assetUpdateDto) throws EntityNotFoundException, InputValidationException {
        assetBean.updateAsset(assetUpdateDto);
    }
}