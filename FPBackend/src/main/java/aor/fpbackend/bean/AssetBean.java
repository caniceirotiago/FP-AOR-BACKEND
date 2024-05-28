package aor.fpbackend.bean;

import aor.fpbackend.dao.AssetDao;
import aor.fpbackend.dao.KeywordDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.AssetEntity;
import aor.fpbackend.entity.KeywordEntity;
import aor.fpbackend.entity.ProjectAssetEntity;
import aor.fpbackend.entity.ProjectEntity;
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
    public void addAsset(AssetAddDto assetAddDto) {
        // Check if the asset already exists, creating it if necessary
        AssetEntity assetEntity;
        if (!assetDao.checkAssetExist(assetAddDto.getName())) {
            assetEntity = new AssetEntity(assetAddDto.getName(), assetAddDto.getType(),
                    assetAddDto.getDescription(), assetAddDto.getQuantity(),
                    assetAddDto.getPartNumber(), assetAddDto.getManufacturer(),
                    assetAddDto.getManufacturerPhone(), assetAddDto.getObservations());
            assetDao.persist(assetEntity);
        } else {
            assetEntity = assetDao.findAssetByName(assetAddDto.getName());
        }
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(assetAddDto.getProjectId());
        // Ensure the projectAssets set is initialized for the assetEntity
        if (assetEntity.getProjectAssets() == null) {
            assetEntity.setProjectAssets(new HashSet<>());
        }
        // Ensure the projectAssets set is initialized
        if (projectEntity.getProjectAssets() == null) {
            projectEntity.setProjectAssets(new HashSet<>());
        }
        // Create a new ProjectAssetEntity
        ProjectAssetEntity projectAssetEntity = new ProjectAssetEntity();
        projectAssetEntity.setAsset(assetEntity);
        projectAssetEntity.setProject(projectEntity);
        projectAssetEntity.setUsedQuantity(assetAddDto.getQuantity());
        // Add the ProjectAssetEntity to the project's projectAssets set
        projectEntity.getProjectAssets().add(projectAssetEntity);
        // Add the ProjectAssetEntity to the asset's projectAssets set
        assetEntity.getProjectAssets().add(projectAssetEntity);
    }

//    public List<AssetGetDto> getAssets() {
//        return convertKeywordEntityListToKeywordDtoList(keywordDao.getAllKeywords());
//    }

//    public List<AssetGetDto> getAssetsByProject(AssetRemoveDto assetRemoveDto) {
//        return convertKeywordEntityListToKeywordDtoList(assetDao.getAssetsByProjectId(assetRemoveDto.getProjectId()));
//    }

//    public List<KeywordGetDto> getKeywordsByFirstLetter(String firstLetter) {
//        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
//            LOGGER.error("Invalid first letter: " + firstLetter);
//            return new ArrayList<>();
//        }
//        return convertKeywordEntityListToKeywordDtoList(keywordDao.getKeywordsByFirstLetter(firstLetter.charAt(0)));
//    }

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
        // Remove the asset from the project's projectAssets
        projectEntity.getProjectAssets().remove(assetEntity);
        // Remove the project from the asset's projectAssets
        assetEntity.getProjectAssets().remove(projectEntity);
    }


//    public KeywordGetDto convertKeywordEntityToKeywordDto(KeywordEntity keywordEntity) {
//        KeywordGetDto keywordGetDto = new KeywordGetDto();
//        keywordGetDto.setId(keywordEntity.getId());
//        keywordGetDto.setName(keywordEntity.getName());
//        return keywordGetDto;
//    }
//
//    public List<KeywordGetDto> convertKeywordEntityListToKeywordDtoList(List<KeywordEntity> keywordEntities) {
//        List<KeywordGetDto> keywordGetDtos = new ArrayList<>();
//        for (KeywordEntity k : keywordEntities) {
//            KeywordGetDto keywordGetDto = convertKeywordEntityToKeywordDto(k);
//            keywordGetDtos.add(keywordGetDto);
//        }
//        return keywordGetDtos;
//    }
}
