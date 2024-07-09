package aor.fpbackend.bean;

import aor.fpbackend.dao.AssetDao;
import aor.fpbackend.dao.ProjectAssetDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Asset.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Project.ProjectAssetGetDto;
import aor.fpbackend.dto.Project.ProjectAssetRemoveDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Stateless
public class AssetBean implements Serializable {
    @EJB
    AssetDao assetDao;
    @EJB
    ProjectDao projectDao;
    @EJB
    UserDao userDao;
    @EJB
    ProjectAssetDao projectAssetDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(AssetBean.class);

    /**
     * Creates a new asset based on the provided AssetCreateDto and associates it with the authenticated user.
     *
     * @param assetCreateDto The DTO containing asset information.
     * @param securityContext The security context providing user authentication details.
     * @throws DuplicatedAttributeException If an asset with the same name already exists.
     * @throws UserNotFoundException If the authenticated user cannot be found in the database.
     */
    public void createAsset(AssetCreateDto assetCreateDto, SecurityContext securityContext) throws DuplicatedAttributeException, UserNotFoundException {
        // Retrieve authenticated user details
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        // Check if the asset name already exists to avoid duplicate entries
        if (assetDao.checkAssetExistByName(assetCreateDto.getName())) {
            throw new DuplicatedAttributeException("Asset name already exists");
        }
        // Create and persist a new AssetEntity object
        AssetEntity assetEntity = new AssetEntity(assetCreateDto.getName(), assetCreateDto.getType(), assetCreateDto.getDescription(), assetCreateDto.getStockQuantity(),
                assetCreateDto.getPartNumber(), assetCreateDto.getManufacturer(), assetCreateDto.getManufacturerPhone(), assetCreateDto.getObservations());
        assetDao.persist(assetEntity);
        // Log successful asset creation and clear logging context
        LOGGER.info("Asset created successfully");
        ThreadContext.clearMap();
    }

    /**
     * Adds an asset to a project with the specified name and updates associated quantities.
     *
     * @param name The name of the asset to add to the project.
     * @param projectId The ID of the project to which the asset is being added.
     * @param usedQuantity The quantity of the asset used in the project.
     * @throws EntityNotFoundException If either the asset or the project cannot be found.
     * @throws ElementAssociationException If the project is in a non-editable state or if the asset is already associated with the project.
     */
    @Transactional
    public void addProjectAssetToProject(String name, long projectId, int usedQuantity) throws EntityNotFoundException, ElementAssociationException {
        // Find the Asset entity by name
        AssetEntity assetEntity = assetDao.findAssetByName(name);
        if (assetEntity == null) {
            throw new EntityNotFoundException("Asset not found");
        }
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Don't add to CANCELLED or FINISHED projects
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState == ProjectStateEnum.CANCELLED || currentState == ProjectStateEnum.FINISHED) {
            throw new ElementAssociationException("Project is not editable anymore");
        }
        // Check if the asset is already associated with the project
        for (ProjectAssetEntity existingProjectAsset : projectEntity.getProjectAssetsForProject()) {
            if (existingProjectAsset.getAsset().equals(assetEntity)) {
                throw new ElementAssociationException("Asset is already associated with the project");
            }
        }
        // Create a new ProjectAssetEntity
        ProjectAssetEntity projectAssetEntity = new ProjectAssetEntity();
        projectAssetEntity.setAsset(assetEntity);
        projectAssetEntity.setProject(projectEntity);
        projectAssetEntity.setUsedQuantity(usedQuantity);
        projectAssetDao.persist(projectAssetEntity);
        // Add the ProjectAssetEntity to the project's projectAssets set
        projectEntity.getProjectAssetsForProject().add(projectAssetEntity);
        // Add the ProjectAssetEntity to the asset's projectAssets set
        assetEntity.getProjectAssets().add(projectAssetEntity);
    }

    /**
     * Retrieves all assets from the database and converts them into AssetGetDto objects.
     *
     * @return A list of AssetGetDto objects representing all assets.
     */
    public List<AssetGetDto> getAllAssets() {
        return convertAssetEntityListToAssetDtoList(assetDao.getAllAssets());
    }

    /**
     * Retrieves project assets associated with the specified project ID from the database
     * and converts them into ProjectAssetGetDto objects.
     *
     * @param projectId The ID of the project for which to retrieve assets.
     * @return A list of ProjectAssetGetDto objects representing project assets.
     */
    public List<ProjectAssetGetDto> getProjectAssetsByProject(long projectId) {
        return convertProjectAssetEntityListToProjectAssetDtoList(projectAssetDao.findProjectAssetsByProjectId(projectId));
    }

    /**
     * Retrieves an asset from the database by its ID and converts it into an AssetGetDto object.
     *
     * @param assetId The ID of the asset to retrieve.
     * @return An AssetGetDto object representing the retrieved asset, or null if not found.
     */
    public AssetGetDto getAssetById(long assetId) {
        return convertAssetEntityToAssetDto(assetDao.findAssetById(assetId));
    }

    /**
     * Retrieves filtered assets based on pagination criteria and converts them into a paginated DTO.
     *
     * @param page The page number (1-indexed) of the results to retrieve.
     * @param pageSize The number of assets per page.
     * @param uriInfo Additional information about the request URI.
     * @return An AssetPaginatedDto object containing a list of AssetGetDto objects for the current page
     *         and total count of assets.
     * @throws InputValidationException If the provided page or pageSize is less than or equal to 0.
     */
    public AssetPaginatedDto getFilteredAssets(int page, int pageSize, UriInfo uriInfo) throws InputValidationException {
        if (page <= 0) {
            throw new InputValidationException("Page must be greater than 0.");
        }
        if (pageSize <= 0) {
            throw new InputValidationException("Page size must be greater than 0.");
        }
        // Retrieve filtered assets from DAO
        List<AssetEntity> assetEntities = assetDao.findFilteredAssets(page, pageSize, uriInfo);
        long totalAssets = assetDao.countFilteredAssets(uriInfo);
        // Convert AssetEntity list to AssetGetDto list
        List<AssetGetDto> assetGetDtos = convertAssetEntityListToAssetDtoList(assetEntities);
        // Return paginated DTO containing assets and total count
        return new AssetPaginatedDto(assetGetDtos, totalAssets);
    }

    /**
     * Retrieves assets whose names start with the specified first letter and converts them into AssetGetDto objects.
     * Returns an empty list if the first letter is null or not a single character.
     *
     * @param firstLetter The first letter of the asset names to filter by.
     * @return A list of AssetGetDto objects representing assets whose names start with the specified first letter,
     *         or an empty list if no matching assets are found.
     */
    public List<AssetGetDto> getAssetsByFirstLetter(String firstLetter) {
        if (firstLetter == null || firstLetter.length() != 1) {
            return new ArrayList<>();
        }
        // Convert first letter to lowercase for case-insensitive comparison
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        // Retrieve assets from DAO based on first letter
        List<AssetEntity> assetEntities = assetDao.getAssetsByFirstLetter(lowerCaseFirstLetter);
        // Convert AssetEntity list to AssetGetDto list
        return convertAssetEntityListToAssetDtoList(assetEntities);
    }

    /**
     * Retrieves a list of all AssetTypeEnum values.
     *
     * @return A list containing all AssetTypeEnum values.
     */
    public List<AssetTypeEnum> getEnumListAssetTypes() {
        List<AssetTypeEnum> assetTypeEnums = new ArrayList<>();
        // Iterate over all AssetTypeEnum values and add them to the list
        for (AssetTypeEnum assetTypeEnum : AssetTypeEnum.values()) {
            assetTypeEnums.add(assetTypeEnum);
        }
        return assetTypeEnums;
    }

    /**
     * Removes an asset from a project by removing the association between the project and the asset.
     * This operation modifies the project's projectAssets set and the asset's projectAssets set accordingly.
     *
     * @param projectAssetRemoveDto The DTO containing project and asset IDs to remove the association.
     * @throws EntityNotFoundException If either the project or the asset with the specified IDs is not found.
     * @throws IllegalStateException If the project does not have the specified asset or if the asset
     *                               does not have the specified project (which should not happen under normal conditions).
     */
    @Transactional
    public void removeProjectAssetFromProject(ProjectAssetRemoveDto projectAssetRemoveDto) throws EntityNotFoundException {
        // Find the project by Id
        ProjectEntity projectEntity = projectDao.findProjectById(projectAssetRemoveDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find the asset by Id
        AssetEntity assetEntity = assetDao.findAssetById(projectAssetRemoveDto.getId());
        if (assetEntity == null) {
            throw new EntityNotFoundException("Asset not found");
        }
        // Remove the ProjectAssetEntity from the project's projectAssets set
        Set<ProjectAssetEntity> projectAssets = projectEntity.getProjectAssetsForProject();
        ProjectAssetEntity projectAssetToRemove = null;
        for (ProjectAssetEntity projectAsset : projectAssets) {
            if (projectAsset.getAsset().equals(assetEntity)) {
                projectAssetToRemove = projectAsset;
                break;
            }
        }
        if (projectAssetToRemove == null) {
            throw new IllegalStateException("Project does not have the specified asset");
        }
        projectAssets.remove(projectAssetToRemove);
        projectEntity.setProjectAssetsForProject(projectAssets);
        // Remove the ProjectAssetEntity from the asset's projectAssets set
        Set<ProjectAssetEntity> assetProjects = assetEntity.getProjectAssets();
        ProjectAssetEntity assetProjectToRemove = null;
        for (ProjectAssetEntity assetProject : assetProjects) {
            if (assetProject.getProject().equals(projectEntity)) {
                assetProjectToRemove = assetProject;
                break;
            }
        }
        if (assetProjectToRemove == null) {
            throw new IllegalStateException("Asset does not have the specified project");
        }
        assetProjects.remove(assetProjectToRemove);
        assetEntity.setProjectAssets(assetProjects);
    }

    /**
     * Updates an existing asset with new information provided in the AssetUpdateDto.
     * Checks for duplicate asset names before updating.
     *
     * @param assetUpdateDto The DTO containing updated information for the asset.
     * @throws EntityNotFoundException If the asset with the specified ID is not found in the database.
     * @throws InputValidationException If a duplicate asset name is detected when updating the asset's name.
     */
    public void updateAsset(AssetUpdateDto assetUpdateDto) throws EntityNotFoundException, InputValidationException {
        // Find existing Asset
        AssetEntity assetEntity = assetDao.findAssetById(assetUpdateDto.getId());
        if (assetEntity == null) {
            throw new EntityNotFoundException("Asset not found with ID: " + assetUpdateDto.getId());
        }
        // When updates project name, check for duplicates
        if (!assetEntity.getName().equalsIgnoreCase(assetUpdateDto.getName())) {
            if (assetDao.checkAssetExistByName(assetUpdateDto.getName())) {
                throw new InputValidationException("Duplicated asset name");
            }
        }
        // Update fields
        assetEntity.setName(assetUpdateDto.getName());
        assetEntity.setType(assetUpdateDto.getType());
        assetEntity.setDescription(assetUpdateDto.getDescription());
        assetEntity.setStockQuantity(assetUpdateDto.getStockQuantity());
        assetEntity.setPartNumber(assetUpdateDto.getPartNumber());
        assetEntity.setManufacturer(assetUpdateDto.getManufacturer());
        assetEntity.setManufacturerPhone(assetUpdateDto.getManufacturerPhone());
        assetEntity.setObservations(assetUpdateDto.getObservations());
        // Log the update information
        LOGGER.info("Asset updated successfully");
        ThreadContext.clearMap();
    }

    /**
     * Converts an AssetEntity object to an AssetGetDto object.
     *
     * @param assetEntity The AssetEntity object to convert.
     * @return The converted AssetGetDto object.
     */
    public AssetGetDto convertAssetEntityToAssetDto(AssetEntity assetEntity) {
        AssetGetDto assetGetDto = new AssetGetDto();
        assetGetDto.setId(assetEntity.getId());
        assetGetDto.setName(assetEntity.getName());
        assetGetDto.setType(assetEntity.getType());
        assetGetDto.setDescription(assetEntity.getDescription());
        assetGetDto.setStockQuantity(assetEntity.getStockQuantity());
        assetGetDto.setPartNumber(assetEntity.getPartNumber());
        assetGetDto.setManufacturer(assetEntity.getManufacturer());
        assetGetDto.setManufacturerPhone(assetEntity.getManufacturerPhone());
        assetGetDto.setObservations(assetEntity.getObservations());
        return assetGetDto;
    }

    /**
     * Converts a list of AssetEntity objects to a list of AssetGetDto objects.
     *
     * @param assetEntities The list of AssetEntity objects to convert.
     * @return The list of converted AssetGetDto objects.
     */
    public List<AssetGetDto> convertAssetEntityListToAssetDtoList(List<AssetEntity> assetEntities) {
        List<AssetGetDto> assetGetDtos = new ArrayList<>();
        for (AssetEntity assetEntity : assetEntities) {
            AssetGetDto assetGetDto = convertAssetEntityToAssetDto(assetEntity);
            assetGetDtos.add(assetGetDto);
        }
        return assetGetDtos;
    }

    /**
     * Converts a ProjectAssetEntity object to a ProjectAssetGetDto object.
     *
     * @param projectAssetEntity The ProjectAssetEntity object to convert.
     * @return The converted ProjectAssetGetDto object.
     */
    public ProjectAssetGetDto convertProjectAssetEntityToProjectAssetDto(ProjectAssetEntity projectAssetEntity) {
        ProjectAssetGetDto projectAssetGetDto = new ProjectAssetGetDto();
        projectAssetGetDto.setId(projectAssetEntity.getAsset().getId());
        projectAssetGetDto.setName(projectAssetEntity.getAsset().getName());
        projectAssetGetDto.setUsedQuantity(projectAssetEntity.getUsedQuantity());
        return projectAssetGetDto;
    }

    /**
     * Converts a list of ProjectAssetEntity objects to a list of ProjectAssetGetDto objects.
     *
     * @param projectAssetEntities The list of ProjectAssetEntity objects to convert.
     * @return The list of converted ProjectAssetGetDto objects.
     */
    public List<ProjectAssetGetDto> convertProjectAssetEntityListToProjectAssetDtoList(List<ProjectAssetEntity> projectAssetEntities) {
        List<ProjectAssetGetDto> projectAssetGetDtos = new ArrayList<>();
        for (ProjectAssetEntity projectAssetEntity : projectAssetEntities) {
            ProjectAssetGetDto projectAssetGetDto = convertProjectAssetEntityToProjectAssetDto(projectAssetEntity);
            projectAssetGetDtos.add(projectAssetGetDto);
        }
        return projectAssetGetDtos;
    }

}
