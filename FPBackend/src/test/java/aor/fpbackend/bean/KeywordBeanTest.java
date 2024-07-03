package aor.fpbackend.bean;

import aor.fpbackend.dao.KeywordDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.Keyword.KeywordRemoveDto;
import aor.fpbackend.entity.KeywordEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.ElementAssociationException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeywordBeanTest {

    @InjectMocks
    private KeywordBean keywordBean;

    @Mock
    private KeywordDao keywordDao;

    @Mock
    private ProjectDao projectDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddKeyword_Success() throws EntityNotFoundException, ElementAssociationException {
        String keywordName = "TestKeyword";
        long projectId = 1L;
        KeywordEntity keywordEntity = new KeywordEntity(keywordName);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setProjectKeywords(new HashSet<>());

        when(keywordDao.findKeywordByName(keywordName)).thenReturn(keywordEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(keywordDao.checkKeywordExist(keywordName)).thenReturn(true);

        keywordBean.addKeyword(keywordName, projectId);

        verify(keywordDao, times(1)).findKeywordByName(keywordName);
        verify(projectDao, times(1)).findProjectById(projectId);
        assertTrue(projectEntity.getProjectKeywords().contains(keywordEntity));
    }

    @Test
    void testAddKeyword_ProjectNotFound() {
        String keywordName = "TestKeyword";
        long projectId = 1L;

        when(projectDao.findProjectById(projectId)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            keywordBean.addKeyword(keywordName, projectId);
        });

        assertEquals("Project not found", thrown.getMessage());
    }

    @Test
    void testAddKeyword_ProjectNotEditable() {
        String keywordName = "TestKeyword";
        long projectId = 1L;
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setState(ProjectStateEnum.FINISHED);

        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        ElementAssociationException thrown = assertThrows(ElementAssociationException.class, () -> {
            keywordBean.addKeyword(keywordName, projectId);
        });

        assertEquals("Project is not editable anymore", thrown.getMessage());
    }

    @Test
    void testRemoveKeyword_Success() throws EntityNotFoundException, InputValidationException {
        long keywordId = 1L;
        long projectId = 1L;
        KeywordRemoveDto keywordRemoveDto = new KeywordRemoveDto(keywordId, projectId);
        KeywordEntity keywordEntity = new KeywordEntity("TestKeyword");
        ProjectEntity projectEntity = new ProjectEntity();
        Set<KeywordEntity> keywords = new HashSet<>();
        keywords.add(keywordEntity);
        projectEntity.setProjectKeywords(keywords);
        Set<ProjectEntity> projects = new HashSet<>();
        projects.add(projectEntity);
        keywordEntity.setProjects(projects);

        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(keywordDao.findKeywordById(keywordId)).thenReturn(keywordEntity);

        keywordBean.removeKeyword(keywordRemoveDto);

        assertFalse(projectEntity.getProjectKeywords().contains(keywordEntity));
        assertFalse(keywordEntity.getProjects().contains(projectEntity));
    }

    @Test
    void testRemoveKeyword_ProjectNotFound() {
        long keywordId = 1L;
        long projectId = 1L;
        KeywordRemoveDto keywordRemoveDto = new KeywordRemoveDto(keywordId, projectId);

        when(projectDao.findProjectById(projectId)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            keywordBean.removeKeyword(keywordRemoveDto);
        });

        assertEquals("Project not found", thrown.getMessage());
    }

    @Test
    void testRemoveKeyword_KeywordNotFound() {
        long keywordId = 1L;
        long projectId = 1L;
        KeywordRemoveDto keywordRemoveDto = new KeywordRemoveDto(keywordId, projectId);
        ProjectEntity projectEntity = new ProjectEntity();

        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(keywordDao.findKeywordById(keywordId)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            keywordBean.removeKeyword(keywordRemoveDto);
        });

        assertEquals("Keyword not found", thrown.getMessage());
    }

    @Test
    void testRemoveKeyword_InputValidationException_NotInProject() {
        long keywordId = 1L;
        long projectId = 1L;
        KeywordRemoveDto keywordRemoveDto = new KeywordRemoveDto(keywordId, projectId);
        KeywordEntity keywordEntity = new KeywordEntity("TestKeyword");
        ProjectEntity projectEntity = new ProjectEntity();

        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(keywordDao.findKeywordById(keywordId)).thenReturn(keywordEntity);

        InputValidationException thrown = assertThrows(InputValidationException.class, () -> {
            keywordBean.removeKeyword(keywordRemoveDto);
        });

        assertEquals("Project does not have the specified keyword", thrown.getMessage());
    }

    @Test
    void testRemoveKeyword_InputValidationException_LastKeyword() {
        long keywordId = 1L;
        long projectId = 1L;
        KeywordRemoveDto keywordRemoveDto = new KeywordRemoveDto(keywordId, projectId);
        KeywordEntity keywordEntity = new KeywordEntity("TestKeyword");
        ProjectEntity projectEntity = new ProjectEntity();
        Set<KeywordEntity> keywords = new HashSet<>();
        keywords.add(keywordEntity);
        projectEntity.setProjectKeywords(keywords);

        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);
        when(keywordDao.findKeywordById(keywordId)).thenReturn(keywordEntity);

        InputValidationException thrown = assertThrows(InputValidationException.class, () -> {
            keywordBean.removeKeyword(keywordRemoveDto);
        });

        assertEquals("Cannot remove the last keyword from the project", thrown.getMessage());
    }
}
