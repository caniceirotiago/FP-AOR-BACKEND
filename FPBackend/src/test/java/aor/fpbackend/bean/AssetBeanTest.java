package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Asset.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Project.ProjectAssetGetDto;
import aor.fpbackend.dto.Project.ProjectAssetRemoveDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.AssetTypeEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.*;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetBeanTest {

    @InjectMocks
    private AssetBean assetBean;

    @Mock
    private AssetDao assetDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ProjectAssetDao projectAssetDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAsset_Success() throws DuplicatedAttributeException, UserNotFoundException, UnknownHostException {
        AssetCreateDto assetCreateDto = new AssetCreateDto("Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("User1");

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(assetDao.checkAssetExistByName("Asset1")).thenReturn(false);
        when(InetAddress.getLocalHost().getHostAddress()).thenReturn("127.0.0.1");

        assetBean.createAsset(assetCreateDto, securityContext);

        verify(assetDao, times(1)).persist(any(AssetEntity.class));
        verify(logger, times(1)).info("Asset created successfully");
    }

    @Test
    void testCreateAsset_UserNotFound() {
        AssetCreateDto assetCreateDto = new AssetCreateDto("Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> assetBean.createAsset(assetCreateDto, securityContext));
    }

    @Test
    void testCreateAsset_DuplicatedAttribute() {
        AssetCreateDto assetCreateDto = new AssetCreateDto("Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(assetDao.checkAssetExistByName("Asset1")).thenReturn(true);

        assertThrows(DuplicatedAttributeException.class, () -> assetBean.createAsset(assetCreateDto, securityContext));
    }

    @Test
    void testAddProjectAssetToProject_Success() throws EntityNotFoundException, ElementAssociationException {
        String assetName = "Asset1";
        long projectId = 1L;
        int usedQuantity = 5;

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setName(assetName);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setProjectAssetsForProject(new HashSet<>());

        when(assetDao.findAssetByName(assetName)).thenReturn(assetEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        assetBean.addProjectAssetToProject(assetName, projectId, usedQuantity);

        verify(projectAssetDao, times(1)).persist(any(ProjectAssetEntity.class));
    }

    @Test
    void testAddProjectAssetToProject_AssetNotFound() {
        String assetName = "Asset1";
        long projectId = 1L;
        int usedQuantity = 5;

        when(assetDao.findAssetByName(assetName)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.addProjectAssetToProject(assetName, projectId, usedQuantity));
    }

    @Test
    void testAddProjectAssetToProject_ProjectNotFound() {
        String assetName = "Asset1";
        long projectId = 1L;
        int usedQuantity = 5;

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setName(assetName);

        when(assetDao.findAssetByName(assetName)).thenReturn(assetEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.addProjectAssetToProject(assetName, projectId, usedQuantity));
    }

    @Test
    void testAddProjectAssetToProject_ProjectCancelledOrFinished() {
        String assetName = "Asset1";
        long projectId = 1L;
        int usedQuantity = 5;

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setName(assetName);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setState(ProjectStateEnum.CANCELLED);

        when(assetDao.findAssetByName(assetName)).thenReturn(assetEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        assertThrows(ElementAssociationException.class, () -> assetBean.addProjectAssetToProject(assetName, projectId, usedQuantity));
    }

    @Test
    void testAddProjectAssetToProject_AssetAlreadyAssociated() {
        String assetName = "Asset1";
        long projectId = 1L;
        int usedQuantity = 5;

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setName(assetName);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setState(ProjectStateEnum.PLANNING);

        ProjectAssetEntity projectAssetEntity = new ProjectAssetEntity();
        projectAssetEntity.setAsset(assetEntity);
        projectEntity.getProjectAssetsForProject().add(projectAssetEntity);

        when(assetDao.findAssetByName(assetName)).thenReturn(assetEntity);
        when(projectDao.findProjectById(projectId)).thenReturn(projectEntity);

        assertThrows(ElementAssociationException.class, () -> assetBean.addProjectAssetToProject(assetName, projectId, usedQuantity));
    }

    @Test
    void testGetAllAssets() {
        List<AssetEntity> assetEntities = new ArrayList<>();
        assetEntities.add(new AssetEntity());

        when(assetDao.getAllAssets()).thenReturn(assetEntities);

        List<AssetGetDto> assetGetDtos = assetBean.getAllAssets();

        assertEquals(1, assetGetDtos.size());
    }

    @Test
    void testGetProjectAssetsByProject() {
        long projectId = 1L;
        List<ProjectAssetEntity> projectAssetEntities = new ArrayList<>();
        projectAssetEntities.add(new ProjectAssetEntity());

        when(projectAssetDao.findProjectAssetsByProjectId(projectId)).thenReturn(projectAssetEntities);

        List<ProjectAssetGetDto> projectAssetGetDtos = assetBean.getProjectAssetsByProject(projectId);

        assertEquals(1, projectAssetGetDtos.size());
    }

    @Test
    void testGetAssetById() {
        long assetId = 1L;
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(assetId);

        when(assetDao.findAssetById(assetId)).thenReturn(assetEntity);

        AssetGetDto assetGetDto = assetBean.getAssetById(assetId);

        assertEquals(assetId, assetGetDto.getId());
    }



    @Test
    void testGetAssetsByFirstLetter() {
        String firstLetter = "A";
        List<AssetEntity> assetEntities = new ArrayList<>();
        assetEntities.add(new AssetEntity());

        when(assetDao.getAssetsByFirstLetter("a")).thenReturn(assetEntities);

        List<AssetGetDto> assetGetDtos = assetBean.getAssetsByFirstLetter(firstLetter);

        assertEquals(1, assetGetDtos.size());
    }

    @Test
    void testGetEnumListAssetTypes() {
        List<AssetTypeEnum> assetTypes = assetBean.getEnumListAssetTypes();

        assertEquals(AssetTypeEnum.values().length, assetTypes.size());
    }

    @Test
    void testRemoveAsset_Success() throws EntityNotFoundException, ElementAssociationException {
        AssetRemoveDto assetRemoveDto = new AssetRemoveDto();
        assetRemoveDto.setAssetId(1L);

        when(projectAssetDao.checkAssetInUse(1L)).thenReturn(false);
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(1L);
        when(assetDao.findAssetById(1L)).thenReturn(assetEntity);

        assetBean.removeAsset(assetRemoveDto);

        verify(assetDao, times(1)).remove(assetEntity);
    }

    @Test
    void testRemoveAsset_AssetInUse() {
        AssetRemoveDto assetRemoveDto = new AssetRemoveDto();
        assetRemoveDto.setAssetId(1L);

        when(projectAssetDao.checkAssetInUse(1L)).thenReturn(true);

        assertThrows(ElementAssociationException.class, () -> assetBean.removeAsset(assetRemoveDto));
    }

    @Test
    void testRemoveAsset_AssetNotFound() {
        AssetRemoveDto assetRemoveDto = new AssetRemoveDto();
        assetRemoveDto.setAssetId(1L);

        when(projectAssetDao.checkAssetInUse(1L)).thenReturn(false);
        when(assetDao.findAssetById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.removeAsset(assetRemoveDto));
    }

    @Test
    void testRemoveProjectAssetFromProject_Success() throws EntityNotFoundException {
        ProjectAssetRemoveDto projectAssetRemoveDto = new ProjectAssetRemoveDto();
        projectAssetRemoveDto.setProjectId(1L);
        projectAssetRemoveDto.setId(1L);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setProjectAssetsForProject(new HashSet<>());

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(1L);

        ProjectAssetEntity projectAssetEntity = new ProjectAssetEntity();
        projectAssetEntity.setProject(projectEntity);
        projectAssetEntity.setAsset(assetEntity);

        projectEntity.getProjectAssetsForProject().add(projectAssetEntity);
        assetEntity.getProjectAssets().add(projectAssetEntity);

        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(assetDao.findAssetById(1L)).thenReturn(assetEntity);

        assetBean.removeProjectAssetFromProject(projectAssetRemoveDto);

        assertFalse(projectEntity.getProjectAssetsForProject().contains(projectAssetEntity));
        assertFalse(assetEntity.getProjectAssets().contains(projectAssetEntity));
    }

    @Test
    void testRemoveProjectAssetFromProject_ProjectNotFound() {
        ProjectAssetRemoveDto projectAssetRemoveDto = new ProjectAssetRemoveDto();
        projectAssetRemoveDto.setProjectId(1L);
        projectAssetRemoveDto.setId(1L);

        when(projectDao.findProjectById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.removeProjectAssetFromProject(projectAssetRemoveDto));
    }

    @Test
    void testRemoveProjectAssetFromProject_AssetNotFound() {
        ProjectAssetRemoveDto projectAssetRemoveDto = new ProjectAssetRemoveDto();
        projectAssetRemoveDto.setProjectId(1L);
        projectAssetRemoveDto.setId(1L);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(1L);

        when(projectDao.findProjectById(1L)).thenReturn(projectEntity);
        when(assetDao.findAssetById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.removeProjectAssetFromProject(projectAssetRemoveDto));
    }

    @Test
    void testUpdateAsset_Success() throws EntityNotFoundException, InputValidationException, UnknownHostException {
        AssetUpdateDto assetUpdateDto = new AssetUpdateDto(1L, "Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(1L);
        assetEntity.setName("OldAsset");

        when(assetDao.findAssetById(1L)).thenReturn(assetEntity);
        when(assetDao.checkAssetExistByName("Asset1")).thenReturn(false);
        when(InetAddress.getLocalHost().getHostAddress()).thenReturn("127.0.0.1");

        assetBean.updateAsset(assetUpdateDto);

        assertEquals("Asset1", assetEntity.getName());
        verify(logger, times(1)).info("Asset updated successfully");
    }

    @Test
    void testUpdateAsset_AssetNotFound() {
        AssetUpdateDto assetUpdateDto = new AssetUpdateDto(1L, "Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");

        when(assetDao.findAssetById(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> assetBean.updateAsset(assetUpdateDto));
    }

    @Test
    void testUpdateAsset_DuplicatedAttribute() {
        AssetUpdateDto assetUpdateDto = new AssetUpdateDto(1L, "Asset1", AssetTypeEnum.COMPONENT, "Description", 10, "PartNumber", "Manufacturer", "123456789", "Observations");

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(1L);
        assetEntity.setName("OldAsset");

        when(assetDao.findAssetById(1L)).thenReturn(assetEntity);
        when(assetDao.checkAssetExistByName("Asset1")).thenReturn(true);

        assertThrows(InputValidationException.class, () -> assetBean.updateAsset(assetUpdateDto));
    }
}
