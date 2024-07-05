package aor.fpbackend.bean;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.User.UserLoginDto;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.JwtKeyProvider;
import aor.fpbackend.utils.PassEncoder;
import io.jsonwebtoken.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.security.Key;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionBeanTest {

    @InjectMocks
    private SessionBean sessionBean;

    @Mock
    private SessionDao sessionDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ConfigurationBean configurationBean;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private ContainerRequestContext requestContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_InvalidCredentials() {
        UserLoginDto userLogin = new UserLoginDto();
        userLogin.setEmail("test@test.com");
        userLogin.setPassword("password");

        when(userDao.findUserByEmail(userLogin.getEmail())).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> sessionBean.login(userLogin));
    }

    @Test
    void testLogout_Success() throws UserNotFoundException {
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        sessionBean.logout(securityContext);

        verify(sessionDao, times(1)).inativateSessionbyAuthToken(authUserDto.getToken());
    }

    @Test
    void testLogout_UserNotFoundException() {
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "", 1L, "username");

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        assertThrows(UserNotFoundException.class, () -> sessionBean.logout(securityContext));
    }

    @Test
    void testCleanupExpiredTokens_Success() throws DatabaseOperationException {
        List<SessionEntity> sessions = new ArrayList<>();
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setTokenExpiration(Instant.now().minusSeconds(10));
        sessionEntity.setActive(true);
        sessions.add(sessionEntity);

        when(sessionDao.findSessionsExpiringInThreeMinutes()).thenReturn(sessions);

        sessionBean.cleanupExpiredTokens();

        verify(sessionDao, times(1)).merge(any(SessionEntity.class));
    }




    @Test
    void testGenerateJwtToken_InvalidInput() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        RoleEntity roleEntity = new RoleEntity(UserRoleEnum.STANDARD_USER);
        userEntity.setRole(roleEntity);

        assertThrows(InputValidationException.class, () -> sessionBean.generateJwtToken(userEntity, -1, "auth"));
    }

    @Test
    void testGenerateNewToken_Success() {
        String token = sessionBean.generateNewToken();
        assertNotNull(token);
    }


    @Test
    void testCreateNewSessionAndInvalidateOld_Success() throws Exception {
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "oldToken", 1L, "username");
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        RoleEntity roleEntity = new RoleEntity(UserRoleEnum.STANDARD_USER);
        userEntity.setRole(roleEntity);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(configurationBean.getConfigValueByKey("sessionTimeout")).thenReturn(60000);

        sessionBean.createNewSessionAndInvalidateOld(authUserDto, requestContext, 60000, "oldToken");

        verify(sessionDao, times(1)).persist(any(SessionEntity.class));
        verify(sessionDao, times(1)).inativateSessionbyAuthToken("oldToken");
        verify(requestContext, times(1)).setProperty(eq("newAuthToken"), anyString());
        verify(requestContext, times(1)).setProperty(eq("newSessionToken"), anyString());
    }

    @Test
    void testCreateInvalidSession_Success() throws Exception {
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");

        sessionBean.createInvalidSession(authUserDto, requestContext);

        verify(sessionDao, times(1)).inativateSessionbyAuthToken(authUserDto.getToken());
        verify(requestContext, times(1)).setProperty("newAuthToken", "null");
        verify(requestContext, times(1)).setProperty("newSessionToken", "null");
    }
}
