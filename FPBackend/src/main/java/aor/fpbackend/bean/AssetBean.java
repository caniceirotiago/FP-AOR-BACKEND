package aor.fpbackend.bean;

import aor.fpbackend.dao.AssetDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.AssetEntity;
import aor.fpbackend.entity.ProjectAssetEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Stateless
public class AssetBean implements Serializable {
    @EJB
    AssetDao assetDao;
    @EJB
    ProjectDao projectDao;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(AssetBean.class);

    @Transactional
    public void addAsset(String name, AssetTypeEnum type, String description, int stockQuantity, String partNumber, String manufacturer,
                         String manufacturerPhone, String observations, long projectId, int usedQuantity) throws EntityNotFoundException {
        // Check if the asset already exists, creating it if necessary
        AssetEntity assetEntity = assetDao.findAssetByName(name);
        if (assetEntity == null) {
            assetEntity = new AssetEntity(name, type, description, stockQuantity,
                    partNumber, manufacturer, manufacturerPhone, observations);
            assetDao.persist(assetEntity);
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

    public List<AssetGetDto> getAssetsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<AssetEntity> assetEntities = assetDao.getAssetsByFirstLetter(lowerCaseFirstLetter);
        return convertAssetEntityListToAssetDtoList(assetEntities);
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

    public AssetGetDto convertAssetEntityToAssetDto(AssetEntity assetEntity) {
        AssetGetDto assetGetDto = new AssetGetDto();
        assetGetDto.setId(assetEntity.getId());
        assetGetDto.setName(assetEntity.getName());
        assetGetDto.setType(assetEntity.getType());
        assetGetDto.setDescription(assetEntity.getDescription());
        assetGetDto.setQuantity(assetEntity.getStockQuantity());
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
