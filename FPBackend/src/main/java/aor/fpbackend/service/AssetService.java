package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.Asset.*;
import aor.fpbackend.dto.Project.ProjectAssetCreateDto;
import aor.fpbackend.dto.Project.ProjectAssetGetDto;
import aor.fpbackend.dto.Project.ProjectAssetRemoveDto;
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
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.UnknownHostException;
import java.util.List;

/**
 * AssetService is a JAX-RS resource class that provides RESTful endpoints for managing assets.
 */
@Path("/assets")
public class AssetService {
    @EJB
    AssetBean assetBean;
    /**
     * Creates a new asset.
     *
     * @param assetCreateDto the AssetCreateDto containing asset details.
     * @param securityContext the SecurityContext containing the security details.
     * @throws DuplicatedAttributeException if the asset already exists.
     * @throws UserNotFoundException if the user is not found.
     */
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.CREATE_ASSET)
    public void createAsset(@Valid AssetCreateDto assetCreateDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException, UserNotFoundException {
        assetBean.createAsset(assetCreateDto, securityContext);
    }

    /**
     * Adds an asset to a project.
     *
     * @param projectAssetCreateDto the ProjectAssetCreateDto containing asset and project details.
     * @throws EntityNotFoundException if the project or asset is not found.
     * @throws ElementAssociationException if there is an issue with the association.
     */
    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_ASSET)
    public void addAssetToProject(@Valid ProjectAssetCreateDto projectAssetCreateDto) throws EntityNotFoundException, ElementAssociationException {
        assetBean.addProjectAssetToProject(projectAssetCreateDto.getName(), projectAssetCreateDto.getProjectId(), projectAssetCreateDto.getUsedQuantity());
    }

    /**
     * Retrieves all assets.
     *
     * @return a list of AssetGetDto representing all assets.
     */

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_ASSETS)
    public List<AssetGetDto> getAllAssets() {
        return assetBean.getAllAssets();
    }

    /**
     * Retrieves filtered assets based on pagination and query parameters.
     *
     * @param page the page number for pagination.
     * @param pageSize the number of items per page.
     * @param uriInfo the UriInfo containing the query parameters.
     * @return an AssetPaginatedDto representing the paginated and filtered assets.
     * @throws InputValidationException if the input data is invalid.
     */
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

    /**
     * Retrieves assets associated with a specific project.
     *
     * @param projectId the ID of the project.
     * @return a list of ProjectAssetGetDto representing the assets of the project.
     */
    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_BY_PROJECT)
    public List<ProjectAssetGetDto> getProjectAssetsByProject(@PathParam("projectId") long projectId) {
        return assetBean.getProjectAssetsByProject(projectId);
    }

    /**
     * Retrieves an asset by its ID.
     *
     * @param assetId the ID of the asset.
     * @return an AssetGetDto representing the asset.
     */
    @GET
    @Path("/id/{assetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_BY_ID)
    public AssetGetDto getAssetById(@PathParam("assetId") long assetId) {
        return assetBean.getAssetById(assetId);
    }

    /**
     * Retrieves assets whose names start with a specific letter.
     *
     * @param firstLetter the initial letter of the asset names.
     * @return a list of AssetGetDto representing the assets.
     */
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSETS_FIRST_LETTER)
    public List<AssetGetDto> getAssetsFirstLetter(@QueryParam("value") String firstLetter) {
        return assetBean.getAssetsByFirstLetter(firstLetter);
    }

    /**
     * Retrieves the list of asset types.
     *
     * @return a list of AssetTypeEnum representing the asset types.
     */
    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSET_ENUMS)
    public List<AssetTypeEnum> getAssetTypes() {
        return assetBean.getEnumListAssetTypes();
    }


    /**
     * Removes an asset from a project.
     *
     * @param projectAssetRemoveDto the ProjectAssetRemoveDto containing asset and project details.
     * @throws EntityNotFoundException if the asset or project is not found.
     */
    @PUT
    @Path("/remove/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeProjectAssetFromProject(@Valid ProjectAssetRemoveDto projectAssetRemoveDto) throws EntityNotFoundException {
        assetBean.removeProjectAssetFromProject(projectAssetRemoveDto);
    }

    /**
     * Updates an existing asset.
     *
     * @param assetUpdateDto the AssetUpdateDto containing the updated asset details.
     * @throws EntityNotFoundException if the asset is not found.
     * @throws InputValidationException if the input data is invalid.
     */
    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASSET_UPDATE)
    public void updateAsset(@Valid AssetUpdateDto assetUpdateDto) throws EntityNotFoundException, InputValidationException {
        assetBean.updateAsset(assetUpdateDto);
    }
}