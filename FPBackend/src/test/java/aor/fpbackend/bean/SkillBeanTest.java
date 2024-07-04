package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Skill.SkillAddUserDto;
import aor.fpbackend.dto.Skill.SkillGetDto;
import aor.fpbackend.dto.Skill.SkillRemoveProjectDto;
import aor.fpbackend.dto.Skill.SkillRemoveUserDto;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.SkillTypeEnum;
import aor.fpbackend.exception.*;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SkillBeanTest {

    @InjectMocks
    private SkillBean skillBean;

    @Mock
    private SkillDao skillDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserDao userDao;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSkillUser_Success() throws Exception {
        SkillAddUserDto skillAddUserDto = new SkillAddUserDto("Java", SkillTypeEnum.SOFTWARE);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        SkillEntity skillEntity = new SkillEntity("Java", SkillTypeEnum.SOFTWARE);
        UserEntity userEntity = new UserEntity();

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(skillDao.findSkillByName(skillAddUserDto.getName())).thenReturn(skillEntity);

        skillBean.addSkillUser(skillAddUserDto, securityContext);

        verify(skillDao, times(1)).findSkillByName(skillAddUserDto.getName());
        verify(userDao, times(1)).findUserById(authUserDto.getUserId());
        assertTrue(userEntity.getUserSkills().contains(skillEntity));
    }

    @Test
    void testAddSkillUser_ThrowsDuplicatedAttributeException() {
        SkillAddUserDto skillAddUserDto = new SkillAddUserDto("Java", SkillTypeEnum.SOFTWARE);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        SkillEntity skillEntity = new SkillEntity("Java", SkillTypeEnum.SOFTWARE);
        UserEntity userEntity = new UserEntity();
        userEntity.setUserSkills(new HashSet<>(Collections.singletonList(skillEntity)));

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(skillDao.findSkillByName(skillAddUserDto.getName())).thenReturn(skillEntity);

        assertThrows(DuplicatedAttributeException.class, () -> {
            skillBean.addSkillUser(skillAddUserDto, securityContext);
        });
    }

    @Test
    void testGetSkills_Success() throws Exception {
        List<SkillEntity> skillEntities = Arrays.asList(new SkillEntity("Java", SkillTypeEnum.SOFTWARE));
        when(skillDao.getAllSkills()).thenReturn(skillEntities);

        List<SkillGetDto> skills = skillBean.getSkills();

        assertEquals(1, skills.size());
        assertEquals("Java", skills.get(0).getName());
    }

    @Test
    void testAddSkillProject_Success() throws Exception {
        String skillName = "Java";
        SkillTypeEnum type = SkillTypeEnum.SOFTWARE;
        long projectId = 1L;
        SkillEntity skillEntity = new SkillEntity(skillName, type);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setState(ProjectStateEnum.IN_PROGRESS);

        when(skillDao.findSkillByName(skillName)).thenReturn(skillEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        skillBean.addSkillProject(skillName, type, projectId);

        verify(skillDao, times(1)).findSkillByName(skillName);
        verify(projectDao, times(1)).findProjectById(projectId);
        assertTrue(projectEntity.getProjectSkills().contains(skillEntity));
    }

    @Test
    void testAddSkillProject_ThrowsElementAssociationException() {
        String skillName = "Java";
        SkillTypeEnum type = SkillTypeEnum.SOFTWARE;
        long projectId = 1L;
        SkillEntity skillEntity = new SkillEntity(skillName, type);
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setState(ProjectStateEnum.CANCELLED);

        when(skillDao.findSkillByName(skillName)).thenReturn(skillEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        assertThrows(ElementAssociationException.class, () -> {
            skillBean.addSkillProject(skillName, type, projectId);
        });
    }

    @Test
    void testGetSkillsByUser_Success() throws Exception {
        String username = "username";
        List<SkillEntity> skillEntities = Arrays.asList(new SkillEntity("Java", SkillTypeEnum.SOFTWARE));
        when(skillDao.getSkillsByUsername(username)).thenReturn(skillEntities);

        List<SkillGetDto> skills = skillBean.getSkillsByUser(username);

        assertEquals(1, skills.size());
        assertEquals("Java", skills.get(0).getName());
    }

    @Test
    void testRemoveSkillUser_Success() throws Exception {
        SkillRemoveUserDto skillRemoveUserDto = new SkillRemoveUserDto(1L);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        SkillEntity skillEntity = new SkillEntity("Java", SkillTypeEnum.SOFTWARE);
        UserEntity userEntity = new UserEntity();
        userEntity.setUserSkills(new HashSet<>(Collections.singletonList(skillEntity)));

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(skillDao.findSkillById(skillRemoveUserDto.getId())).thenReturn(skillEntity);

        skillBean.removeSkillUser(skillRemoveUserDto, securityContext);

        assertFalse(userEntity.getUserSkills().contains(skillEntity));
    }

    @Test
    void testGetEnumListSkillTypes_Success() {
        List<SkillTypeEnum> skillTypes = skillBean.getEnumListSkillTypes();

        assertEquals(4, skillTypes.size());
        assertTrue(skillTypes.contains(SkillTypeEnum.KNOWLEDGE));
        assertTrue(skillTypes.contains(SkillTypeEnum.SOFTWARE));
        assertTrue(skillTypes.contains(SkillTypeEnum.HARDWARE));
        assertTrue(skillTypes.contains(SkillTypeEnum.TOOLS));
    }


}
