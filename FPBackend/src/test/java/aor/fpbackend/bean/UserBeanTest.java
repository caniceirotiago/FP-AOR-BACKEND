package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Email.EmailDto;
import aor.fpbackend.dto.Password.PasswordRequestResetDto;
import aor.fpbackend.dto.Password.PasswordResetDto;
import aor.fpbackend.dto.Password.PasswordUpdateDto;
import aor.fpbackend.dto.User.*;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import aor.fpbackend.utils.PassEncoder;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

class UserBeanTest {

    @InjectMocks
    private UserBean userBean;
    @Mock
    private UserDao userDao;
    @Mock
    private RoleDao roleDao;
    @Mock
    private LaboratoryDao labDao;
    @Mock
    private EmailService emailService;
    @Mock
    private PassEncoder passEncoder;
    @Mock
    private SessionBean sessionBean;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ProjectMembershipDao projectMemberDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userBean = new UserBean();

        userDao = mock(UserDao.class);
        roleDao = mock(RoleDao.class);
        labDao = mock(LaboratoryDao.class);
        projectMemberDao = mock(ProjectMembershipDao.class);
        emailService = mock(EmailService.class);
        passEncoder = mock(PassEncoder.class);
        sessionBean = mock(SessionBean.class);
        securityContext = mock(SecurityContext.class);

        userBean.userDao = userDao;
        userBean.roleDao = roleDao;
        userBean.labDao = labDao;
        userBean.emailService = emailService;
        userBean.passEncoder = passEncoder;
        userBean.sessionBean = sessionBean;
        userBean.projectMemberDao = projectMemberDao;
    }

    @Test
    void testRegister_Success() throws InvalidCredentialsException, EntityNotFoundException {
        // Arrange
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setUsername("testUser");
        userRegisterDto.setPassword("password");
        userRegisterDto.setFirstName("Test");
        userRegisterDto.setLastName("User");
        userRegisterDto.setLaboratoryId(1L);

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(UserRoleEnum.STANDARD_USER);

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);
        labEntity.setLocation(LocationEnum.TOMAR);

        when(userDao.checkEmailAndUsernameExist("test@example.com", "testUser")).thenReturn(false);
        when(roleDao.findRoleByName(UserRoleEnum.STANDARD_USER)).thenReturn(roleEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);
        when(passEncoder.encode("password")).thenReturn("encodedPassword");
        when(sessionBean.generateNewToken()).thenReturn("confirmationToken");

        ArgumentCaptor<UserEntity> userEntityCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userBean.register(userRegisterDto);

        // Assert
        verify(userDao).persist(userEntityCaptor.capture());
        UserEntity persistedUser = userEntityCaptor.getValue();

        assertEquals("test@example.com", persistedUser.getEmail());
        assertEquals("testUser", persistedUser.getUsername());
        assertEquals("encodedPassword", persistedUser.getPassword());
        assertEquals(roleEntity, persistedUser.getRole());
        assertEquals(labEntity, persistedUser.getLaboratory());
        assertEquals("confirmationToken", persistedUser.getConfirmationToken());
        assertFalse(persistedUser.isConfirmed());
    }

    @Test
    void testRegister_EmailOrUsernameExists() {
        // Arrange
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setUsername("testUser");

        when(userDao.checkEmailAndUsernameExist("test@example.com", "testUser")).thenReturn(true);

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(
                InvalidCredentialsException.class,
                () -> userBean.register(userRegisterDto),
                "Expected register() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Email or username already exists"));
    }

    @Test
    void testRegister_RoleNotFound() {
        // Arrange
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setUsername("testUser");
        userRegisterDto.setLaboratoryId(1L);

        when(userDao.checkEmailAndUsernameExist("test@example.com", "testUser")).thenReturn(false);
        when(roleDao.findRoleByName(UserRoleEnum.STANDARD_USER)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> userBean.register(userRegisterDto),
                "Expected register() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Default role not found"));
    }

    @Test
    void testRegister_LaboratoryNotFound() {
        // Arrange
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setUsername("testUser");
        userRegisterDto.setLaboratoryId(1L);

        when(userDao.checkEmailAndUsernameExist("test@example.com", "testUser")).thenReturn(false);
        when(roleDao.findRoleByName(UserRoleEnum.STANDARD_USER)).thenReturn(new RoleEntity());
        when(labDao.findLaboratoryById(1L)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> userBean.register(userRegisterDto),
                "Expected register() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Laboratory not found"));
    }
    @Test
    void testConfirmUser_Success() throws InputValidationException, UserNotFoundException {
        // Arrange
        String token = "validToken";
        UserEntity userEntity = new UserEntity();
        userEntity.setConfirmationToken(token);
        userEntity.setConfirmationTokenTimestamp(Instant.now());

        when(userDao.findUserByConfirmationToken(token)).thenReturn(userEntity);

        // Act
        userBean.confirmUser(token);

        // Assert
        verify(userDao, times(1)).findUserByConfirmationToken(token);
        assertTrue(userEntity.isConfirmed());
        assertNull(userEntity.getConfirmationToken());
        assertNull(userEntity.getConfirmationTokenTimestamp());
    }

    @Test
    void testConfirmUser_InvalidToken() {
        // Arrange
        String token = "";

        // Act & Assert
        InputValidationException thrown = assertThrows(
                InputValidationException.class,
                () -> userBean.confirmUser(token),
                "Expected confirmUser() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Invalid token"));
    }

    @Test
    void testConfirmUser_UserNotFound() {
        // Arrange
        String token = "invalidToken";
        when(userDao.findUserByConfirmationToken(token)).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.confirmUser(token),
                "Expected confirmUser() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to confirm user with invalid token"));
    }

    @Test
    void testConfirmUser_TokenExpired() {
        // Arrange
        String token = "expiredToken";
        UserEntity userEntity = new UserEntity();
        userEntity.setConfirmationToken(token);
        userEntity.setConfirmationTokenTimestamp(Instant.now().minus(25, ChronoUnit.HOURS)); // Assuming expiration is 24 hours

        when(userDao.findUserByConfirmationToken(token)).thenReturn(userEntity);

        // Act & Assert
        InputValidationException thrown = assertThrows(
                InputValidationException.class,
                () -> userBean.confirmUser(token),
                "Expected confirmUser() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Token expired"));
    }
    @Test
    void testRequestPasswordReset_Success() throws UserNotFoundException, ForbiddenAccessException {
        // Arrange
        PasswordRequestResetDto passwordRequestResetDto = new PasswordRequestResetDto();
        passwordRequestResetDto.setEmail("test@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testUser");

        when(userDao.findUserByEmail("test@example.com")).thenReturn(userEntity);
        when(sessionBean.generateNewToken()).thenReturn("resetToken");

        // Act
        userBean.requestPasswordReset(passwordRequestResetDto);

        // Assert
        verify(userDao, times(1)).findUserByEmail("test@example.com");
        verify(sessionBean, times(1)).generateNewToken();
        verify(emailService, times(1)).sendPasswordResetEmail("test@example.com", "resetToken");

        assertNotNull(userEntity.getResetPasswordToken());
        assertEquals("resetToken", userEntity.getResetPasswordToken());
        assertNotNull(userEntity.getResetPasswordTimestamp());
    }

    @Test
    void testRequestPasswordReset_UserNotFound() {
        // Arrange
        PasswordRequestResetDto passwordRequestResetDto = new PasswordRequestResetDto();
        passwordRequestResetDto.setEmail("test@example.com");

        when(userDao.findUserByEmail("test@example.com")).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.requestPasswordReset(passwordRequestResetDto),
                "Expected requestPasswordReset() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to reset password with invalid credentials!"));
    }

    @Test
    void testRequestPasswordReset_ForbiddenAccess() {
        // Arrange
        PasswordRequestResetDto passwordRequestResetDto = new PasswordRequestResetDto();
        passwordRequestResetDto.setEmail("test@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testUser");
        userEntity.setResetPasswordTimestamp(Instant.now().plus(5, ChronoUnit.MINUTES)); // Password reset requested recently

        when(userDao.findUserByEmail("test@example.com")).thenReturn(userEntity);

        // Act & Assert
        ForbiddenAccessException thrown = assertThrows(
                ForbiddenAccessException.class,
                () -> userBean.requestPasswordReset(passwordRequestResetDto),
                "Expected requestPasswordReset() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("You can only make a password reset request every"));
    }
    @Test
    void testRequestNewConfirmationEmail_Success() throws InvalidRequestOnRegistConfirmationException {
        // Arrange
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testUser");
        userEntity.setConfirmed(false);
        userEntity.setConfirmationToken("confirmationToken");

        when(userDao.findUserByEmail("test@example.com")).thenReturn(userEntity);

        // Act
        userBean.requestNewConfirmationEmail(emailDto);

        // Assert
        verify(userDao, times(1)).findUserByEmail("test@example.com");
        verify(emailService, times(1)).sendConfirmationEmail("test@example.com", "confirmationToken");

        assertNotNull(userEntity.getLastSentEmailTimestamp());
    }

    @Test
    void testRequestNewConfirmationEmail_UserNotFound() {
        // Arrange
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");

        when(userDao.findUserByEmail("test@example.com")).thenReturn(null);

        // Act & Assert
        InvalidRequestOnRegistConfirmationException thrown = assertThrows(
                InvalidRequestOnRegistConfirmationException.class,
                () -> userBean.requestNewConfirmationEmail(emailDto),
                "Expected requestNewConfirmationEmail() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to request new confirmation email with user not found"));
    }

    @Test
    void testRequestNewConfirmationEmail_UserAlreadyConfirmed() {
        // Arrange
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail("test@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testUser");
        userEntity.setConfirmed(true);

        when(userDao.findUserByEmail("test@example.com")).thenReturn(userEntity);

        // Act & Assert
        InvalidRequestOnRegistConfirmationException thrown = assertThrows(
                InvalidRequestOnRegistConfirmationException.class,
                () -> userBean.requestNewConfirmationEmail(emailDto),
                "Expected requestNewConfirmationEmail() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to request new confirmation email with user already confirmed"));
    }


    @Test
    void testResetPassword_Success() throws IllegalStateException, UserNotFoundException, ForbiddenAccessException {
        // Arrange
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setResetToken("validToken");
        passwordResetDto.setNewPassword("newPassword");

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setResetPasswordTimestamp(Instant.now().plus(1, ChronoUnit.HOURS)); // Token is valid

        when(userDao.findUserByResetPasswordToken("validToken")).thenReturn(userEntity);
        when(passEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        userBean.resetPassword(passwordResetDto);

        // Assert
        verify(userDao, times(1)).findUserByResetPasswordToken("validToken");
        verify(passEncoder, times(1)).encode("newPassword");
        assertEquals("encodedNewPassword", userEntity.getPassword());
        assertNull(userEntity.getResetPasswordToken());
        assertNull(userEntity.getResetPasswordTimestamp());
    }

    @Test
    void testResetPassword_UserNotFound() {
        // Arrange
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setResetToken("invalidToken");

        when(userDao.findUserByResetPasswordToken("invalidToken")).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.resetPassword(passwordResetDto),
                "Expected resetPassword() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to reset password with invalid token"));
    }

    @Test
    void testResetPassword_TokenExpired() {
        // Arrange
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setResetToken("expiredToken");

        UserEntity userEntity = new UserEntity();
        userEntity.setResetPasswordTimestamp(Instant.now().minus(1, ChronoUnit.HOURS)); // Token is expired

        when(userDao.findUserByResetPasswordToken("expiredToken")).thenReturn(userEntity);

        // Act & Assert
        ForbiddenAccessException thrown = assertThrows(
                ForbiddenAccessException.class,
                () -> userBean.resetPassword(passwordResetDto),
                "Expected resetPassword() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Attempt to reset password with expired token"));
    }
    @Test
    void testUpdateUserProfile_Success() throws UserNotFoundException, EntityNotFoundException, DatabaseOperationException {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);
        labEntity.setLocation(LocationEnum.TOMAR);

        UserUpdateDto updatedUser = new UserUpdateDto();
        updatedUser.setFirstName("NewFirstName");
        updatedUser.setLastName("NewLastName");
        updatedUser.setPhoto("newPhotoUrl");
        updatedUser.setLaboratoryId(1L);
        updatedUser.setBiography("New Biography");
        updatedUser.setPrivate(true);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);

        // Act
        userBean.updateUserProfile(securityContext, updatedUser);

        // Assert
        verify(userDao, times(1)).findUserById(1L);
        verify(labDao, times(1)).findLaboratoryById(1L);
        verify(userDao, times(1)).merge(userEntity);

        assertEquals("NewFirstName", userEntity.getFirstName());
        assertEquals("NewLastName", userEntity.getLastName());
        assertEquals("newPhotoUrl", userEntity.getPhoto());
        assertEquals("New Biography", userEntity.getBiography());
        assertTrue(userEntity.isPrivate());
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserUpdateDto updatedUser = new UserUpdateDto();
        updatedUser.setLaboratoryId(1L);

        when(userDao.findUserById(1L)).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.updateUserProfile(securityContext, updatedUser),
                "Expected updateUserProfile() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User not found"));
    }

    @Test
    void testUpdateUserProfile_LaboratoryNotFound() {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");

        UserUpdateDto updatedUser = new UserUpdateDto();
        updatedUser.setLaboratoryId(1L);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> userBean.updateUserProfile(securityContext, updatedUser),
                "Expected updateUserProfile() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Laboratory not found"));
    }

    @Test
    void testUpdateUserProfile_DatabaseOperationException() {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");

        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);
        labEntity.setLocation(LocationEnum.TOMAR);

        UserUpdateDto updatedUser = new UserUpdateDto();
        updatedUser.setLaboratoryId(1L);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(labDao.findLaboratoryById(1L)).thenReturn(labEntity);
        doThrow(new PersistenceException("Error")).when(userDao).merge(any(UserEntity.class));

        // Act & Assert
        DatabaseOperationException thrown = assertThrows(
                DatabaseOperationException.class,
                () -> userBean.updateUserProfile(securityContext, updatedUser),
                "Expected updateUserProfile() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Persistence error while updating user profile"));
    }
    @Test
    void testGetUserBasicInfo_Success() throws UserNotFoundException {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPhoto("photoUrl");

        when(userDao.findUserById(1L)).thenReturn(userEntity);

        // Act
        UserBasicInfoDto userBasicInfoDto = userBean.getUserBasicInfo(securityContext);

        // Assert
        verify(userDao, times(1)).findUserById(1L);
        assertNotNull(userBasicInfoDto);
        assertEquals(1L, userBasicInfoDto.getId());
        assertEquals("testUser", userBasicInfoDto.getUsername());
        assertEquals("photoUrl", userBasicInfoDto.getPhoto());
    }

    @Test
    void testGetUserBasicInfo_UserNotFound() {
        // Arrange
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        when(userDao.findUserById(1L)).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.getUserBasicInfo(securityContext),
                "Expected getUserBasicInfo() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User not found"));
    }
    @Test
    void testGetUserBasicInfoByUsername_Success() throws UserNotFoundException {
        // Arrange
        String username = "testUser";

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername(username);
        userEntity.setPhoto("photoUrl");

        when(userDao.findUserByUsername(username)).thenReturn(userEntity);

        // Act
        UserBasicInfoDto userBasicInfoDto = userBean.getUserBasicInfo(username);

        // Assert
        verify(userDao, times(1)).findUserByUsername(username);
        assertNotNull(userBasicInfoDto);
        assertEquals(1L, userBasicInfoDto.getId());
        assertEquals(username, userBasicInfoDto.getUsername());
        assertEquals("photoUrl", userBasicInfoDto.getPhoto());
    }

    @Test
    void testGetUserBasicInfoByUsername_UserNotFound() {
        // Arrange
        String username = "testUser";

        when(userDao.findUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.getUserBasicInfo(username),
                "Expected getUserBasicInfo() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User not found"));
    }

    @Test
    void testGetUsersBasicInfoByFirstLetter_Success() {
        // Arrange
        String firstLetter = "a";

        List<UserEntity> userEntities = new ArrayList<>();
        UserEntity userEntity1 = new UserEntity();
        userEntity1.setId(1L);
        userEntity1.setUsername("alice");
        userEntity1.setPhoto("photo1");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setUsername("adam");
        userEntity2.setPhoto("photo2");

        userEntities.add(userEntity1);
        userEntities.add(userEntity2);

        when(userDao.getUsersByFirstLetter("a")).thenReturn(userEntities);

        // Act
        List<UserBasicInfoDto> usersBasicInfo = userBean.getUsersBasicInfoByFirstLetter(firstLetter);

        // Assert
        verify(userDao, times(1)).getUsersByFirstLetter("a");
        assertNotNull(usersBasicInfo);
        assertEquals(2, usersBasicInfo.size());

        UserBasicInfoDto userBasicInfoDto1 = usersBasicInfo.get(0);
        assertEquals(1L, userBasicInfoDto1.getId());
        assertEquals("alice", userBasicInfoDto1.getUsername());
        assertEquals("photo1", userBasicInfoDto1.getPhoto());

        UserBasicInfoDto userBasicInfoDto2 = usersBasicInfo.get(1);
        assertEquals(2L, userBasicInfoDto2.getId());
        assertEquals("adam", userBasicInfoDto2.getUsername());
        assertEquals("photo2", userBasicInfoDto2.getPhoto());
    }


    @Test
    void testGetUsersBasicInfoByFirstLetter_Exception() {
        // Arrange
        String firstLetter = "a";
        when(userDao.getUsersByFirstLetter("a")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> userBean.getUsersBasicInfoByFirstLetter(firstLetter),
                "Expected getUsersBasicInfoByFirstLetter() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Database error"));
    }
    @Test
    void testGetUserEmailRecipientByFirstLetter_Success() {
        // Arrange
        String firstLetter = "a";

        List<UserEntity> userEntities = new ArrayList<>();
        UserEntity userEntity1 = new UserEntity();
        userEntity1.setId(1L);
        userEntity1.setUsername("alice");
        userEntity1.setFirstName("Alice");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setUsername("adam");
        userEntity2.setFirstName("Adam");

        userEntities.add(userEntity1);
        userEntities.add(userEntity2);

        when(userDao.getUsersByFirstLetterUsernameOrFirstName("a")).thenReturn(userEntities);

        // Act
        List<UserMessageInfoDto> userMessageInfoDtos = userBean.getUserEmailRecipientByFirstLetter(firstLetter);

        // Assert
        verify(userDao, times(1)).getUsersByFirstLetterUsernameOrFirstName("a");
        assertNotNull(userMessageInfoDtos);
        assertEquals(2, userMessageInfoDtos.size());

        UserMessageInfoDto userMessageInfoDto1 = userMessageInfoDtos.get(0);
        assertEquals(1L, userMessageInfoDto1.getId());
        assertEquals("alice", userMessageInfoDto1.getUsername());
        assertEquals("Alice", userMessageInfoDto1.getFirstName());

        UserMessageInfoDto userMessageInfoDto2 = userMessageInfoDtos.get(1);
        assertEquals(2L, userMessageInfoDto2.getId());
        assertEquals("adam", userMessageInfoDto2.getUsername());
        assertEquals("Adam", userMessageInfoDto2.getFirstName());
    }


    @Test
    void testGetUserEmailRecipientByFirstLetter_Exception() {
        // Arrange
        String firstLetter = "a";
        when(userDao.getUsersByFirstLetterUsernameOrFirstName("a")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> userBean.getUserEmailRecipientByFirstLetter(firstLetter),
                "Expected getUserEmailRecipientByFirstLetter() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Database error"));
    }
    @Test
    void testGetProfileDto_Success() throws UserNotFoundException, ForbiddenAccessException {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("testuser@example.com");
        userEntity.setUsername(username);
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setPhoto("photo_url");
        userEntity.setBiography("This is a test user");

        LaboratoryEntity laboratory = new LaboratoryEntity();
        laboratory.setId(1L);
        userEntity.setLaboratory(laboratory);

        when(userDao.findUserByUsername(username)).thenReturn(userEntity);
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        // Act
        UserProfileDto userProfileDto = userBean.getProfileDto(username, securityContext);

        // Assert
        assertNotNull(userProfileDto);
        assertEquals(1L, userProfileDto.getId());
        assertEquals("testuser@example.com", userProfileDto.getEmail());
        assertEquals(username, userProfileDto.getUsername());
        assertEquals("Test", userProfileDto.getFirstName());
        assertEquals("User", userProfileDto.getLastName());
        assertEquals("photo_url", userProfileDto.getPhoto());
        assertEquals("This is a test user", userProfileDto.getBiography());
        assertEquals(1L, userProfileDto.getLaboratoryId());
    }

    @Test
    void testGetProfileDto_LaboratoryNull() {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("testuser@example.com");
        userEntity.setUsername(username);
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setPhoto("photo_url");
        userEntity.setBiography("This is a test user");

        // Laboratory is not set, simulating a null laboratory
        when(userDao.findUserByUsername(username)).thenReturn(userEntity);
        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setUserId(1L);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            userBean.getProfileDto(username, securityContext);
        });
    }

    @Test
    void testUpdatePassword_Success() throws UserNotFoundException, InputValidationException, IllegalStateException {
        // Arrange
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("newPassword");

        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setPassword("encodedOldPassword");

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(passEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passEncoder.matches("newPassword", "encodedOldPassword")).thenReturn(false);
        when(passEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        userBean.updatePassword(passwordUpdateDto, securityContext);

        // Assert
        verify(userDao, times(1)).findUserById(1L);
        verify(passEncoder, times(1)).matches("oldPassword", "encodedOldPassword");
        verify(passEncoder, times(1)).encode("newPassword");
        assertEquals("encodedNewPassword", userEntity.getPassword());
    }

    @Test
    void testUpdatePassword_UserNotFound() {
        // Arrange
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        when(userDao.findUserById(1L)).thenReturn(null);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userBean.updatePassword(passwordUpdateDto, securityContext),
                "Expected updatePassword() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User not found"));
    }

    @Test
    void testUpdatePassword_InvalidOldPassword() {
        // Arrange
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("newPassword");

        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setPassword("encodedOldPassword");

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(passEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        InputValidationException thrown = assertThrows(
                InputValidationException.class,
                () -> userBean.updatePassword(passwordUpdateDto, securityContext),
                "Expected updatePassword() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Invalid old password or repeated new password"));
    }

    @Test
    void testUpdatePassword_EncodeNewPasswordFailure() {
        // Arrange
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("newPassword");

        AuthUserDto authUserDto = mock(AuthUserDto.class);
        when(authUserDto.getUserId()).thenReturn(1L);
        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setPassword("encodedOldPassword");

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(passEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passEncoder.matches("newPassword", "encodedOldPassword")).thenReturn(false);
        doThrow(new PersistenceException("Error")).when(passEncoder).encode("newPassword");

        // Act & Assert
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> userBean.updatePassword(passwordUpdateDto, securityContext),
                "Expected updatePassword() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Error while updating password"));
    }
    @Test
    void testOldPasswordConfirmation_Success() throws Exception {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("hashedOldPassword");

        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("newPassword");

        when(passEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);
        when(passEncoder.matches("newPassword", "hashedOldPassword")).thenReturn(false);

        // Act
        Method method = UserBean.class.getDeclaredMethod("oldPasswordConfirmation", UserEntity.class, PasswordUpdateDto.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(userBean, userEntity, passwordUpdateDto);

        // Assert
        assertTrue(result);
        verify(passEncoder, times(1)).matches("oldPassword", "hashedOldPassword");
        verify(passEncoder, times(1)).matches("newPassword", "hashedOldPassword");
    }

    @Test
    void testOldPasswordConfirmation_InvalidOldPassword() throws Exception {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("hashedOldPassword");

        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("newPassword");

        when(passEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(false);

        // Act
        Method method = UserBean.class.getDeclaredMethod("oldPasswordConfirmation", UserEntity.class, PasswordUpdateDto.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(userBean, userEntity, passwordUpdateDto);

        // Assert
        assertFalse(result);
        verify(passEncoder, times(1)).matches("oldPassword", "hashedOldPassword");
        verify(passEncoder, times(0)).matches("newPassword", "hashedOldPassword");
    }

    @Test
    void testOldPasswordConfirmation_NewPasswordSameAsOldPassword() throws Exception {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("hashedOldPassword");

        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword("oldPassword");
        passwordUpdateDto.setNewPassword("oldPassword");

        when(passEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);
        when(passEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);

        // Act
        Method method = UserBean.class.getDeclaredMethod("oldPasswordConfirmation", UserEntity.class, PasswordUpdateDto.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(userBean, userEntity, passwordUpdateDto);

        // Assert
        assertFalse(result);
        verify(passEncoder, times(2)).matches("oldPassword", "hashedOldPassword");
    }


    @Test
    void testUpdateRole_Success() throws InvalidCredentialsException, EntityNotFoundException, UnknownHostException {
        // Arrange
        UserUpdateRoleDto userUpdateRoleDto = new UserUpdateRoleDto();
        userUpdateRoleDto.setUserId(1L);
        userUpdateRoleDto.setRoleId(2L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(2L);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(roleDao.findRoleById(2L)).thenReturn(roleEntity);

        // Act
        userBean.updateRole(userUpdateRoleDto);

        // Assert
        verify(userDao, times(1)).findUserById(1L);
        verify(roleDao, times(1)).findRoleById(2L);
        assertEquals(roleEntity, userEntity.getRole());
    }

    @Test
    void testUpdateRole_UserNotFound() {
        // Arrange
        UserUpdateRoleDto userUpdateRoleDto = new UserUpdateRoleDto();
        userUpdateRoleDto.setUserId(1L);
        userUpdateRoleDto.setRoleId(2L);

        when(userDao.findUserById(1L)).thenReturn(null);

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(
                InvalidCredentialsException.class,
                () -> userBean.updateRole(userUpdateRoleDto),
                "Expected updateRole() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User not found with this username"));
    }

    @Test
    void testUpdateRole_RoleNotFound() {
        // Arrange
        UserUpdateRoleDto userUpdateRoleDto = new UserUpdateRoleDto();
        userUpdateRoleDto.setUserId(1L);
        userUpdateRoleDto.setRoleId(2L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(roleDao.findRoleById(2L)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> userBean.updateRole(userUpdateRoleDto),
                "Expected updateRole() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Role not found with this Id"));
    }

    @Test
    void testUpdateRole_PersistenceException() {
        // Arrange
        UserUpdateRoleDto userUpdateRoleDto = new UserUpdateRoleDto();
        userUpdateRoleDto.setUserId(1L);
        userUpdateRoleDto.setRoleId(2L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        RoleEntity newRole = new RoleEntity();
        newRole.setId(2L);

        when(userDao.findUserById(1L)).thenReturn(userEntity);
        when(roleDao.findRoleById(2L)).thenReturn(newRole);
        doThrow(new PersistenceException("Error")).when(userDao).merge(any(UserEntity.class));

        // Act & Assert
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> userBean.updateRole(userUpdateRoleDto),
                "Expected updateRole() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Error while updating user role"));
    }

    @Test
    void testCreateDefaultUserIfNotExistent_Success() throws DatabaseOperationException {
        // Arrange
        String username = "testUser";
        String photo = "photoUrl";
        long roleId = 1L;
        long labId = 1L;

        when(userDao.checkUsernameExist(username)).thenReturn(false);
        when(passEncoder.encode(username)).thenReturn("encodedPassword");

        LaboratoryEntity labEntity = new LaboratoryEntity();
        when(labDao.findLaboratoryById(labId)).thenReturn(labEntity);

        RoleEntity roleEntity = new RoleEntity();
        when(roleDao.findRoleById(roleId)).thenReturn(roleEntity);

        // Act
        userBean.createDefaultUserIfNotExistent(username, photo, roleId, labId);

        // Assert
        verify(userDao, times(1)).checkUsernameExist(username);
        verify(passEncoder, times(1)).encode(username);
        verify(labDao, times(1)).findLaboratoryById(labId);
        verify(roleDao, times(1)).findRoleById(roleId);

        ArgumentCaptor<UserEntity> userEntityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao, times(1)).persist(userEntityCaptor.capture());
        UserEntity persistedUser = userEntityCaptor.getValue();

        assertEquals(username + "@" + username + ".com", persistedUser.getEmail());
        assertEquals("encodedPassword", persistedUser.getPassword());
        assertEquals(username, persistedUser.getUsername());
        assertEquals("fn_" + username, persistedUser.getFirstName());
        assertEquals(photo, persistedUser.getPhoto());
        assertEquals(labEntity, persistedUser.getLaboratory());
        assertEquals(roleEntity, persistedUser.getRole());
    }

    @Test
    void testCreateDefaultUserIfNotExistent_UserAlreadyExists() throws DatabaseOperationException {
        // Arrange
        String username = "testUser";
        when(userDao.checkUsernameExist(username)).thenReturn(true);

        // Act
        userBean.createDefaultUserIfNotExistent(username, "photoUrl", 1L, 1L);

        // Assert
        verify(userDao, times(1)).checkUsernameExist(username);
        verify(passEncoder, times(0)).encode(anyString());
        verify(labDao, times(0)).findLaboratoryById(anyLong());
        verify(roleDao, times(0)).findRoleById(anyLong());
        verify(userDao, times(0)).persist(any(UserEntity.class));
    }

    @Test
    void testCreateDefaultUserIfNotExistent_LaboratoryNotFound() {
        // Arrange
        String username = "testUser";
        long labId = 1L;

        when(userDao.checkUsernameExist(username)).thenReturn(false);
        when(labDao.findLaboratoryById(labId)).thenReturn(null);

        // Act & Assert
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> userBean.createDefaultUserIfNotExistent(username, "photoUrl", 1L, labId),
                "Expected createDefaultUserIfNotExistent() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Laboratory not found."));
    }

    @Test
    void testCreateDefaultUserIfNotExistent_RoleNotFound() {
        // Arrange
        String username = "testUser";
        long labId = 1L;
        long roleId = 1L;

        when(userDao.checkUsernameExist(username)).thenReturn(false);
        LaboratoryEntity labEntity = new LaboratoryEntity();
        when(labDao.findLaboratoryById(labId)).thenReturn(labEntity);
        when(roleDao.findRoleById(roleId)).thenReturn(null);

        // Act & Assert
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> userBean.createDefaultUserIfNotExistent(username, "photoUrl", roleId, labId),
                "Expected createDefaultUserIfNotExistent() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Role not found."));
    }

    @Test
    void testCreateDefaultUserIfNotExistent_PersistenceException() {
        // Arrange
        String username = "testUser";
        String photo = "photoUrl";
        long roleId = 1L;
        long labId = 1L;

        when(userDao.checkUsernameExist(username)).thenReturn(false);
        when(passEncoder.encode(username)).thenReturn("encodedPassword");

        LaboratoryEntity labEntity = new LaboratoryEntity();
        when(labDao.findLaboratoryById(labId)).thenReturn(labEntity);

        RoleEntity roleEntity = new RoleEntity();
        when(roleDao.findRoleById(roleId)).thenReturn(roleEntity);

        doThrow(new PersistenceException("Error")).when(userDao).persist(any(UserEntity.class));

        // Act & Assert
        DatabaseOperationException thrown = assertThrows(
                DatabaseOperationException.class,
                () -> userBean.createDefaultUserIfNotExistent(username, photo, roleId, labId),
                "Expected createDefaultUserIfNotExistent() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Error while creating default user"));
    }
    @Test
    void testConvertUserRegisterDtoToUserEntity_Success() {
        // Arrange
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("test@example.com");
        userRegisterDto.setUsername("testUser");
        userRegisterDto.setFirstName("Test");
        userRegisterDto.setLastName("User");

        // Act
        UserEntity userEntity = userBean.convertUserRegisterDtotoUserEntity(userRegisterDto);

        // Assert
        assertNotNull(userEntity);
        assertEquals("test@example.com", userEntity.getEmail());
        assertEquals("testUser", userEntity.getUsername());
        assertEquals("Test", userEntity.getFirstName());
        assertEquals("User", userEntity.getLastName());
    }
    @Test
    void testConvertUserEntityToUserProfileDto_Success() {
        // Arrange
        LaboratoryEntity labEntity = new LaboratoryEntity();
        labEntity.setId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testUser");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setPhoto("photoUrl");
        userEntity.setBiography("This is a biography");
        userEntity.setLaboratory(labEntity);
        userEntity.setPrivate(true);

        // Act
        UserProfileDto userProfileDto = userBean.convertUserEntitytoUserProfileDto(userEntity);

        // Assert
        assertNotNull(userProfileDto);
        assertEquals(1L, userProfileDto.getId());
        assertEquals("test@example.com", userProfileDto.getEmail());
        assertEquals("testUser", userProfileDto.getUsername());
        assertEquals("Test", userProfileDto.getFirstName());
        assertEquals("User", userProfileDto.getLastName());
        assertEquals("photoUrl", userProfileDto.getPhoto());
        assertEquals("This is a biography", userProfileDto.getBiography());
        assertEquals(1L, userProfileDto.getLaboratoryId());
        assertTrue(userProfileDto.isPrivate());
    }
    @Test
    void testConvertUserEntityToUserBasicInfoDto_Success() {
        // Arrange
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1L);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPhoto("photoUrl");
        userEntity.setRole(roleEntity);

        // Act
        UserBasicInfoDto userBasicInfoDto = userBean.convertUserEntitytoUserBasicInfoDto(userEntity);

        // Assert
        assertNotNull(userBasicInfoDto);
        assertEquals(1L, userBasicInfoDto.getId());
        assertEquals("testUser", userBasicInfoDto.getUsername());
        assertEquals("photoUrl", userBasicInfoDto.getPhoto());
        assertEquals(1L, userBasicInfoDto.getRole());
    }
    @Test
    void testConvertUserEntityListToUserBasicInfoDtoList_Success() {
        // Arrange
        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setId(1L);

        UserEntity userEntity1 = new UserEntity();
        userEntity1.setId(1L);
        userEntity1.setUsername("testUser1");
        userEntity1.setPhoto("photoUrl1");
        userEntity1.setRole(roleEntity1);

        RoleEntity roleEntity2 = new RoleEntity();
        roleEntity2.setId(2L);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setUsername("testUser2");
        userEntity2.setPhoto("photoUrl2");
        userEntity2.setRole(roleEntity2);

        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(userEntity1);
        userEntities.add(userEntity2);

        // Act
        List<UserBasicInfoDto> userBasicInfoDtos = userBean.convertUserEntityListToUserBasicInfoDtoList(userEntities);

        // Assert
        assertNotNull(userBasicInfoDtos);
        assertEquals(2, userBasicInfoDtos.size());

        UserBasicInfoDto userBasicInfoDto1 = userBasicInfoDtos.get(0);
        assertEquals(1L, userBasicInfoDto1.getId());
        assertEquals("testUser1", userBasicInfoDto1.getUsername());
        assertEquals("photoUrl1", userBasicInfoDto1.getPhoto());
        assertEquals(1L, userBasicInfoDto1.getRole());

        UserBasicInfoDto userBasicInfoDto2 = userBasicInfoDtos.get(1);
        assertEquals(2L, userBasicInfoDto2.getId());
        assertEquals("testUser2", userBasicInfoDto2.getUsername());
        assertEquals("photoUrl2", userBasicInfoDto2.getPhoto());
        assertEquals(2L, userBasicInfoDto2.getRole());
    }
}
