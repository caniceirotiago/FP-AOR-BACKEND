package aor.fpbackend.bean;

import aor.fpbackend.dao.KeywordDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.KeywordAddDto;
import aor.fpbackend.dto.KeywordGetDto;
import aor.fpbackend.dto.KeywordRemoveDto;
import aor.fpbackend.dto.ProjectDto;
import aor.fpbackend.entity.KeywordEntity;
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
import java.util.Set;
import java.util.List;


@Stateless
public class KeywordBean implements Serializable {
    @EJB
    KeywordDao keywordDao;
    @EJB
    ProjectDao projectDao;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(KeywordBean.class);

    @Transactional
    public void addKeyword(KeywordAddDto keywordAddDto, ProjectDto projectDto) {
        // Ensure the keyword exists, creating it if necessary
        checkKeywordExist(keywordAddDto.getName());
        // Find the keyword by name
        KeywordEntity keywordEntity = keywordDao.findKeywordByName(keywordAddDto.getName());
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(projectDto.getId());
        // Add the keyword to the project's keywords
        Set<KeywordEntity> projectKeywords = projectEntity.getProjectKeywords();
        if (projectKeywords == null) {
            projectKeywords = new HashSet<>();
        }
        if (!projectKeywords.contains(keywordEntity)) {
            projectKeywords.add(keywordEntity);
            projectEntity.setProjectKeywords(projectKeywords);
        }
        // Add the project to the keyword's projects
        Set<ProjectEntity> keywordProjects = keywordEntity.getProjects();
        if (keywordProjects == null) {
            keywordProjects = new HashSet<>();
        }
        if (!keywordProjects.contains(projectEntity)) {
            keywordProjects.add(projectEntity);
            keywordEntity.setProjects(keywordProjects);
        }
    }

    private void checkKeywordExist(String name) {
        if (!keywordDao.checkKeywordExist(name)) {
            KeywordEntity keyword = new KeywordEntity(name);
            keywordDao.persist(keyword);
        }
    }

    public List<KeywordGetDto> getKeywords() {
        return convertKeywordEntityListToKeywordDtoList(keywordDao.getAllKeywords());
    }

    public List<KeywordGetDto> getKeywordsByProject(ProjectDto projectDto) {
        return convertKeywordEntityListToKeywordDtoList(keywordDao.getKeywordsByProjectId(projectDto.getId()));
    }

    public List<KeywordGetDto> getKeywordsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        return convertKeywordEntityListToKeywordDtoList(keywordDao.getKeywordsByFirstLetter(firstLetter.charAt(0)));
    }

    @Transactional
    public void removeKeyword(KeywordRemoveDto keywordRemoveDto, ProjectDto projectDto) throws EntityNotFoundException {
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(projectDto.getId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find the keyword by Id
        KeywordEntity keywordEntity = keywordDao.findKeywordById(keywordRemoveDto.getId());
        if (keywordEntity == null) {
            throw new EntityNotFoundException("Keyword not found");
        }
        // Remove the keyword from the project's keywords
        Set<KeywordEntity> projectKeywords = projectEntity.getProjectKeywords();
        if (projectKeywords.contains(keywordEntity)) {
            projectKeywords.remove(keywordEntity);
            projectEntity.setProjectKeywords(projectKeywords);

            // Remove the project from the keyword's projects
            Set<ProjectEntity> keywordProjects = keywordEntity.getProjects();
            keywordProjects.remove(projectEntity);
            keywordEntity.setProjects(keywordProjects);
        } else {
            throw new IllegalStateException("Project does not have the specified keyword");
        }
    }

    public KeywordGetDto convertKeywordEntityToKeywordDto(KeywordEntity keywordEntity) {
        KeywordGetDto keywordGetDto = new KeywordGetDto();
        keywordGetDto.setId(keywordEntity.getId());
        keywordGetDto.setName(keywordEntity.getName());
        return keywordGetDto;
    }

    public List<KeywordGetDto> convertKeywordEntityListToKeywordDtoList(List<KeywordEntity> keywordEntities) {
        List<KeywordGetDto> keywordGetDtos = new ArrayList<>();
        for (KeywordEntity k : keywordEntities) {
            KeywordGetDto keywordGetDto = convertKeywordEntityToKeywordDto(k);
            keywordGetDtos.add(keywordGetDto);
        }
        return keywordGetDtos;
    }
}