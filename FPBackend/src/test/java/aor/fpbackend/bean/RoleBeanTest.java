package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleBeanTest {

    @InjectMocks
    private RoleBean roleBean;

    @Mock
    private RoleDao roleDao;

    @Mock
    private MethodDao methodDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRoleIfNotExists_RoleDoesNotExist() {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;

        when(roleDao.checkRoleExist(roleEnum)).thenReturn(false);

        roleBean.createRoleIfNotExists(roleEnum);

        verify(roleDao, times(1)).persist(any(RoleEntity.class));
        verify(roleDao, times(1)).checkRoleExist(roleEnum);
    }

    @Test
    void testCreateRoleIfNotExists_RoleExists() {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;

        when(roleDao.checkRoleExist(roleEnum)).thenReturn(true);

        roleBean.createRoleIfNotExists(roleEnum);

        verify(roleDao, times(1)).checkRoleExist(roleEnum);
        verify(roleDao, times(0)).persist(any(RoleEntity.class));
    }

    @Test
    void testCreateRoleIfNotExists_Exception() {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;

        when(roleDao.checkRoleExist(roleEnum)).thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> roleBean.createRoleIfNotExists(roleEnum));

        verify(roleDao, times(1)).checkRoleExist(roleEnum);
        verify(roleDao, times(0)).persist(any(RoleEntity.class));
    }

    @Test
    void testAddPermission_Success() throws DatabaseOperationException {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;
        MethodEnum methodEnum = MethodEnum.ADD_SKILL_USER;

        RoleEntity roleEntity = new RoleEntity(roleEnum);
        MethodEntity methodEntity = new MethodEntity(methodEnum);

        when(roleDao.findRoleByName(roleEnum)).thenReturn(roleEntity);
        when(methodDao.findMethodByName(methodEnum)).thenReturn(methodEntity);

        roleBean.addPermission(roleEnum, methodEnum);

        verify(roleDao, times(1)).findRoleByName(roleEnum);
        verify(methodDao, times(1)).findMethodByName(methodEnum);
        assertTrue(roleEntity.getMethods().contains(methodEntity));
        assertTrue(methodEntity.getRoles().contains(roleEntity));
    }

    @Test
    void testAddPermission_RoleOrMethodNotFound() {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;
        MethodEnum methodEnum = MethodEnum.ADD_SKILL_USER;

        when(roleDao.findRoleByName(roleEnum)).thenReturn(null);
        when(methodDao.findMethodByName(methodEnum)).thenReturn(null);

        assertThrows(DatabaseOperationException.class, () -> roleBean.addPermission(roleEnum, methodEnum));

        verify(roleDao, times(1)).findRoleByName(roleEnum);
        verify(methodDao, times(1)).findMethodByName(methodEnum);
    }

    @Test
    void testAddPermission_Exception() {
        UserRoleEnum roleEnum = UserRoleEnum.ADMIN;
        MethodEnum methodEnum = MethodEnum.ADD_SKILL_USER;

        when(roleDao.findRoleByName(roleEnum)).thenThrow(new RuntimeException("Database error"));

        assertThrows(DatabaseOperationException.class, () -> roleBean.addPermission(roleEnum, methodEnum));

        verify(roleDao, times(1)).findRoleByName(roleEnum);
    }
}
