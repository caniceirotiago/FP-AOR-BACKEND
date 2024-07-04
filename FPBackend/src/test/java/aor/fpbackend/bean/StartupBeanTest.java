package aor.fpbackend.bean;

import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.utils.GlobalSettings;
import jakarta.ejb.EJB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StartupBeanTest {

    @Mock
    private RoleBean roleBean;

    @Mock
    private LaboratoryBean labBean;

    @Mock
    private UserBean userBean;

    @Mock
    private ConfigurationBean configBean;

    @Mock
    private MethodBean methodBean;

    @InjectMocks
    private StartupBean startupBean;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitSuccess() throws DatabaseOperationException {
        doNothing().when(roleBean).createRoleIfNotExists(any(UserRoleEnum.class));
        doNothing().when(labBean).createLaboratoryIfNotExists(any(LocationEnum.class));
        doNothing().when(userBean).createDefaultUserIfNotExistent(anyString(), anyString(), anyInt(), anyInt());
        doNothing().when(configBean).createDefaultConfigIfNotExistent(anyString(), anyInt());
        doNothing().when(methodBean).createMethodIfNotExistent(any(MethodEnum.class), anyString(), anyLong());
        doNothing().when(roleBean).addPermission(any(UserRoleEnum.class), any(MethodEnum.class));

        startupBean.init();

        verify(roleBean, times(1)).createRoleIfNotExists(UserRoleEnum.ADMIN);
        verify(roleBean, times(1)).createRoleIfNotExists(UserRoleEnum.STANDARD_USER);
        verify(labBean, times(1)).createLaboratoryIfNotExists(LocationEnum.LISBOA);
        verify(labBean, times(1)).createLaboratoryIfNotExists(LocationEnum.COIMBRA);
        verify(userBean, times(1)).createDefaultUserIfNotExistent("admin", "https://i.pinimg.com/474x/7e/71/9b/7e719be79d55353a3ce6551d704e43ca.jpg", 1, 2);
        verify(configBean, times(1)).createDefaultConfigIfNotExistent("sessionTimeout", 36000000);
        verify(methodBean, times(1)).createMethodIfNotExistent(MethodEnum.UPDATE_ROLE, "updates user role", MethodEnum.UPDATE_ROLE.getValue());
        verify(roleBean, times(1)).addPermission(UserRoleEnum.ADMIN, MethodEnum.UPDATE_ROLE);
    }

    @Test
    void testInitDatabaseOperationException() throws DatabaseOperationException {
        doThrow(new DatabaseOperationException("Error creating roles")).when(roleBean).createRoleIfNotExists(any(UserRoleEnum.class));

        assertThrows(DatabaseOperationException.class, () -> startupBean.init());

        verify(roleBean, times(1)).createRoleIfNotExists(UserRoleEnum.ADMIN);
    }


    @Test
    void testCreateRoles() throws DatabaseOperationException {
        invokePrivateMethod("createRoles");
        verify(roleBean, times(1)).createRoleIfNotExists(UserRoleEnum.ADMIN);
        verify(roleBean, times(1)).createRoleIfNotExists(UserRoleEnum.STANDARD_USER);
    }

    @Test
    void testCreateLaboratories() throws DatabaseOperationException {
        invokePrivateMethod("createLaboratories");
        verify(labBean, times(1)).createLaboratoryIfNotExists(LocationEnum.LISBOA);
        verify(labBean, times(1)).createLaboratoryIfNotExists(LocationEnum.COIMBRA);
    }

    @Test
    void testCreateUsers() throws DatabaseOperationException {
        invokePrivateMethod("createUsers");
        verify(userBean, times(1)).createDefaultUserIfNotExistent("admin", "https://i.pinimg.com/474x/7e/71/9b/7e719be79d55353a3ce6551d704e43ca.jpg", 1, 2);
        verify(userBean, times(1)).createDefaultUserIfNotExistent("standardUser", "https://i.pinimg.com/474x/0a/a8/58/0aa8581c2cb0aa948d63ce3ddad90c81.jpg", 2, 2);
    }

    @Test
    void testCreateDefaultConfigs() throws DatabaseOperationException {
        invokePrivateMethod("createDefaultConfigs");
        verify(configBean, times(1)).createDefaultConfigIfNotExistent("sessionTimeout", GlobalSettings.DEFAULT_SESSION_TIMEOUT_MILLIS);
        verify(configBean, times(1)).createDefaultConfigIfNotExistent("maxProjectMembers", GlobalSettings.DEFAULT_NUMBER_MEMBERS_PER_PROJECT);
    }

    @Test
    void testCreateMethods() throws DatabaseOperationException {
        invokePrivateMethod("createMethods");
        verify(methodBean, times(1)).createMethodIfNotExistent(MethodEnum.UPDATE_ROLE, "updates user role", MethodEnum.UPDATE_ROLE.getValue());
    }

    @Test
    void testAddPermissions() throws DatabaseOperationException {
        invokePrivateMethod("addPermissions");
        verify(roleBean, times(1)).addPermission(UserRoleEnum.ADMIN, MethodEnum.UPDATE_ROLE);
        verify(roleBean, times(1)).addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_SKILL_USER);
    }

    private void invokePrivateMethod(String methodName) throws DatabaseOperationException {
        try {
            Method method = StartupBean.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(startupBean);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
