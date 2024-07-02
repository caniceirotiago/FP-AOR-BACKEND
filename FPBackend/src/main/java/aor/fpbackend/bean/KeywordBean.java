package aor.fpbackend.bean;

import aor.fpbackend.dao.KeywordDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.Keyword.KeywordGetDto;
import aor.fpbackend.dto.Keyword.KeywordRemoveDto;
import aor.fpbackend.entity.KeywordEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.ElementAssociationException;
import aor.fpbackend.exception.EntityNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * KeywordBean is a stateless session bean responsible for managing keywords and their associations with projects.
 * <p>
 * This bean handles the creation of keywords if they do not already exist,
 * as well as the addition and removal of keywords from projects.
 * It also provides methods for retrieving keywords.
 * </p>
 * <p>
 * Key functionalities provided by this bean include:
 * <ul>
 *     <li>Adding keywords to projects.</li>
 *     <li>Removing keywords from projects.</li>
 *     <li>Retrieving keywords and converting them to DTOs (Data Transfer Objects).</li>
 * </ul>
 * </p>
 * <p>
 * The class uses dependency injection to obtain instances of KeywordDao and ProjectDao,
 * promoting a clean architecture.
 * </p>
 * <p>
 * Technologies Used:
 * <ul>
 *     <li><b>Jakarta EE</b>: For EJB and transaction management.</li>
 *     <li><b>Log4j</b>: For logging operations.</li>
 * </ul>
 * </p>
 * <p>
 * Note: This bean is thread-safe as it is a stateless session bean.
 * </p>
 */
@Stateless
public class KeywordBean implements Serializable {
    @EJB
    KeywordDao keywordDao;
    @EJB
    ProjectDao projectDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(KeywordBean.class);

    /**
     * Adds a keyword to a project, ensuring the keyword exists by creating it if necessary.
     *
     * @param keywordName the name of the keyword
     * @param projectId   the ID of the project
     * @throws EntityNotFoundException if the project is not found
     */
    @Transactional
    public void addKeyword(String keywordName, long projectId) throws EntityNotFoundException, ElementAssociationException {
        checkKeywordExist(keywordName);
        KeywordEntity keywordEntity = keywordDao.findKeywordByName(keywordName);
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if(projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Don't add to CANCELLED or FINISHED projects
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState == ProjectStateEnum.CANCELLED || currentState == ProjectStateEnum.FINISHED) {
            throw new ElementAssociationException("Project is not editable anymore");
        }
        Set<KeywordEntity> projectKeywords = projectEntity.getProjectKeywords();
        if (projectKeywords == null) {
            projectKeywords = new HashSet<>();
        }
        if (!projectKeywords.contains(keywordEntity)) {
            projectKeywords.add(keywordEntity);
            projectEntity.setProjectKeywords(projectKeywords);
        }
        Set<ProjectEntity> keywordProjects = keywordEntity.getProjects();
        if (keywordProjects == null) {
            keywordProjects = new HashSet<>();
        }
        if (!keywordProjects.contains(projectEntity)) {
            keywordProjects.add(projectEntity);
            keywordEntity.setProjects(keywordProjects);
        }
    }
    /**
     * Checks if a keyword exists and creates it if it doesn't.
     *
     * @param name the name of the keyword
     */
    private void checkKeywordExist(String name) {
        if (!keywordDao.checkKeywordExist(name)) {
            KeywordEntity keyword = new KeywordEntity(name);
            keywordDao.persist(keyword);
        }
    }

    /**
     * Retrieves all keywords and converts them to a list of KeywordGetDto.
     *
     * @return a list of KeywordGetDto objects
     */
    public List<KeywordGetDto> getKeywords() {
        return convertKeywordEntityListToKeywordDtoList(keywordDao.getAllKeywords());
    }

    /**
     * Retrieves all keywords associated with a specific project and converts them to a list of KeywordGetDto.
     *
     * @param projectId the ID of the project
     * @return a list of KeywordGetDto objects
     */
    public List<KeywordGetDto> getKeywordsByProject(long projectId) {
        return convertKeywordEntityListToKeywordDtoList(keywordDao.getKeywordsByProjectId(projectId));
    }

    /**
     * Retrieves all keywords that start with a specific letter and converts them to a list of KeywordGetDto.
     *
     * @param firstLetter the first letter of the keywords to retrieve
     * @return a list of KeywordGetDto objects
     */
    public List<KeywordGetDto> getKeywordsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<KeywordEntity> keywordEntities = keywordDao.getKeywordsByFirstLetter(lowerCaseFirstLetter);
        return convertKeywordEntityListToKeywordDtoList(keywordEntities);
    }

    /**
     * Removes a keyword from a project.
     *
     * @param keywordRemoveDto the DTO containing the keyword ID and project ID
     * @throws EntityNotFoundException if the keyword or project is not found
     */
    @Transactional
    public void removeKeyword(KeywordRemoveDto keywordRemoveDto) throws EntityNotFoundException {
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(keywordRemoveDto.getProjectId());
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
            Set<ProjectEntity> keywordProjects = keywordEntity.getProjects();
            keywordProjects.remove(projectEntity);
            keywordEntity.setProjects(keywordProjects);
        } else {
            throw new IllegalStateException("Project does not have the specified keyword");
        }
    }

    /**
     * Converts a KeywordEntity to a KeywordGetDto.
     *
     * @param keywordEntity the KeywordEntity to convert
     * @return the corresponding KeywordGetDto
     */
    public KeywordGetDto convertKeywordEntityToKeywordDto(KeywordEntity keywordEntity) {
        KeywordGetDto keywordGetDto = new KeywordGetDto();
        keywordGetDto.setId(keywordEntity.getId());
        keywordGetDto.setName(keywordEntity.getName());
        return keywordGetDto;
    }

    /**
     * Converts a list of KeywordEntity objects to a list of KeywordGetDto objects.
     *
     * @param keywordEntities the list of KeywordEntity objects to convert
     * @return a list of KeywordGetDto objects
     */
    public List<KeywordGetDto> convertKeywordEntityListToKeywordDtoList(List<KeywordEntity> keywordEntities) {
        List<KeywordGetDto> keywordGetDtos = new ArrayList<>();
        for (KeywordEntity k : keywordEntities) {
            KeywordGetDto keywordGetDto = convertKeywordEntityToKeywordDto(k);
            keywordGetDtos.add(keywordGetDto);
        }
        return keywordGetDtos;
    }
}