package aor.fpbackend.bean;

import aor.fpbackend.dao.AssetDao;
import aor.fpbackend.dao.ProjectAssetDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    ProjectAssetDao projectAssetDao;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(AssetBean.class);


    public void createAsset(AssetCreateDto assetCreateDto) throws InputValidationException, DuplicatedAttributeException {
        if (assetCreateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        // Check if the asset name already exists to avoid duplicate entries
        if (assetDao.checkAssetExistByName(assetCreateDto.getName())) {
            throw new DuplicatedAttributeException("Asset name already exists");
        }
        AssetEntity assetEntity = new AssetEntity(assetCreateDto.getName(), assetCreateDto.getType(), assetCreateDto.getDescription(), assetCreateDto.getStockQuantity(),
                assetCreateDto.getPartNumber(), assetCreateDto.getManufacturer(), assetCreateDto.getManufacturerPhone(), assetCreateDto.getObservations());
        assetDao.persist(assetEntity);
    }

    @Transactional
    public void addAssetToProject(String name, long projectId, int usedQuantity) throws EntityNotFoundException {
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
        // Check if the asset is already associated with the project
        for (ProjectAssetEntity existingProjectAsset : projectEntity.getProjectAssetsForProject()) {
            if (existingProjectAsset.getAsset().equals(assetEntity)) {
                throw new IllegalStateException("Asset is already associated with the project");
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

    public List<AssetGetDto> getAllAssets() {
        return convertAssetEntityListToAssetDtoList(assetDao.getAllAssets());
    }

    public List<AssetGetDto> getAssetsByProject(long projectId) {
        return convertAssetEntityListToAssetDtoList(assetDao.getAssetsByProjectId(projectId));
    }

    public AssetGetDto getAssetById(long assetId) {
        return convertAssetEntityToAssetDto(assetDao.findAssetById(assetId));
    }

    public AssetsPaginatedDto getFilteredAssets(int page, int pageSize, UriInfo uriInfo) {
        List<AssetEntity> assetEntities = assetDao.findFilteredAssets(page, pageSize, uriInfo);
        long totalAssets = assetDao.countFilteredAssets(uriInfo);
        List<AssetGetDto> assetGetDtos = convertAssetEntityListToAssetDtoList(assetEntities);
        return new AssetsPaginatedDto(assetGetDtos, totalAssets);
    }

    public List<AssetGetDto> getAssetsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<AssetEntity> assetEntities = assetDao.getAssetsByFirstLetter(lowerCaseFirstLetter);
        return convertAssetEntityListToAssetDtoList(assetEntities);
    }

    public List<AssetTypeEnum> getEnumListAssetTypes() {
        List<AssetTypeEnum> assetTypeEnums = new ArrayList<>();
        for (AssetTypeEnum assetTypeEnum : AssetTypeEnum.values()) {
            assetTypeEnums.add(assetTypeEnum);
        }
        return assetTypeEnums;
    }

    @Transactional
    public void removeAsset(AssetRemoveDto assetRemoveDto) throws EntityNotFoundException {
        // Find the project by Id
        ProjectEntity projectEntity = projectDao.findProjectById(assetRemoveDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find the asset by Id
        AssetEntity assetEntity = assetDao.findAssetById(assetRemoveDto.getId());
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

    public void updateAsset(AssetUpdateDto assetUpdateDto) throws EntityNotFoundException, InputValidationException {
        // Find existing Asset
        AssetEntity assetEntity = assetDao.findAssetById(assetUpdateDto.getId());
        if (assetEntity == null) {
            throw new EntityNotFoundException("Asset not found with ID: " + assetUpdateDto.getId());
        }
        // Validate DTO
        if (assetUpdateDto == null) {
            throw new InputValidationException("Invalid DTO");
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
    }

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

    public List<AssetGetDto> convertAssetEntityListToAssetDtoList(List<AssetEntity> assetEntities) {
        List<AssetGetDto> assetGetDtos = new ArrayList<>();
        for (AssetEntity assetEntity : assetEntities) {
            AssetGetDto assetGetDto = convertAssetEntityToAssetDto(assetEntity);
            assetGetDtos.add(assetGetDto);
        }
        return assetGetDtos;
    }

}
