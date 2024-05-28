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

    private static final Logger LOGGER = LogManager.getLogger(aor.fpbackend.bean.KeywordBean.class);

    @Transactional
    public void addAsset(AssetAddDto assetAddDto) {
        // Ensure the asset exists, creating it if necessary
        if (!assetDao.checkAssetExist(assetAddDto.getName())) {
            AssetEntity asset = new AssetEntity(assetAddDto.getName(), assetAddDto.getType(),
                    assetAddDto.getDescription(), assetAddDto.getQuantity(), assetAddDto.getPartNumber(), assetAddDto.getManufacturer(),
                    assetAddDto.getManufacturerPhone(), assetAddDto.getObservations());
            assetDao.persist(asset);
        }
        // Find the asset by name
        AssetEntity assetEntity = assetDao.findAssetByName(assetAddDto.getName());
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(assetAddDto.getProjectId());
        // Add the asset to the project's asset
//        Set<ProjectAssetEntity> projectAssets = projectEntity.getProjectAssets();
//        if (projectAssets == null) {
//            projectAssets = new HashSet<>();
//        }
//        if (!projectAssets.contains(assetEntity)) {
//            projectAssets.add(assetEntity);
//            projectEntity.setProjectKeywords(projectAssets);
//        }
//        // Add the project to the asset's projects
//        Set<ProjectEntity> assetProjects = assetEntity.getProjectAssets();
//        if (assetProjects == null) {
//            assetProjects = new HashSet<>();
//        }
//        if (!assetProjects.contains(projectEntity)) {
//            assetProjects.add(projectEntity);
//            assetEntity.setProjects(assetProjects);
//        }
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

//    @Transactional
//    public void removeKeyword(KeywordRemoveDto keywordRemoveDto) throws EntityNotFoundException {
//        // Find the project by id
//        ProjectEntity projectEntity = projectDao.findProjectById(keywordRemoveDto.getProjectId());
//        if (projectEntity == null) {
//            throw new EntityNotFoundException("Project not found");
//        }
//        // Find the keyword by Id
//        KeywordEntity keywordEntity = keywordDao.findKeywordById(keywordRemoveDto.getId());
//        if (keywordEntity == null) {
//            throw new EntityNotFoundException("Keyword not found");
//        }
//        // Remove the keyword from the project's keywords
//        Set<KeywordEntity> projectKeywords = projectEntity.getProjectKeywords();
//        if (projectKeywords.contains(keywordEntity)) {
//            projectKeywords.remove(keywordEntity);
//            projectEntity.setProjectKeywords(projectKeywords);
//
//            // Remove the project from the keyword's projects
//            Set<ProjectEntity> keywordProjects = keywordEntity.getProjects();
//            keywordProjects.remove(projectEntity);
//            keywordEntity.setProjects(keywordProjects);
//        } else {
//            throw new IllegalStateException("Project does not have the specified keyword");
//        }
//    }

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
