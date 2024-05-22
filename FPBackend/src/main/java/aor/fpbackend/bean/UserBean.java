package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(UserBean.class);

    @EJB
    UserDao userDao;
    @EJB
    PassEncoder passEncoder;
    @EJB
    SessionDao sessionDao;
    @EJB
    RoleDao roleDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    EmailService emailService;
    @EJB
    ConfigurationDao configDao;


    public void createDefaultUserIfNotExistent(String username, long roleId, long labId) throws DatabaseOperationException {
        if (!userDao.checkUsernameExist(username)) {
            String email = username + "@" + username + ".com";
            String encryptedPassword = passEncoder.encode(username);
            LaboratoryEntity laboratory = labDao.findLaboratoryById(labId);
            if (laboratory == null) {
                throw new IllegalStateException("Laboratory not found.");
            }
            RoleEntity role = roleDao.findRoleById(roleId);
            if (role == null) {
                throw new IllegalStateException("Role not found.");
            }
            UserEntity userEntity = new UserEntity(email, encryptedPassword, username, username, username, true, false, true, laboratory, role);
            userDao.persist(userEntity);
        }
    }


    public void register(UserDto user) throws InvalidCredentialsException, UnknownHostException {
        if (user == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with invalid credentials!");
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (userDao.checkEmailExist(user.getEmail()) || userDao.checkUsernameExist(user.getUsername())) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with invalid credentials!");
            throw new InvalidCredentialsException("Invalid credentials - Email or username already exists");
        }

        try {
            UserEntity newUserEntity = convertUserDtotoUserEntity(user);
            String encryptedPassword = passEncoder.encode(user.getPassword());
            newUserEntity.setPassword(encryptedPassword);
            // Retrieve and set the default role "Standard User"
            RoleEntity role = roleDao.findRoleById(2);
            if (role == null) {
                throw new IllegalStateException("Default role not found.");
            }
            newUserEntity.setRole(role);
            // Retrieve and set the laboratory
            LaboratoryEntity lab = labDao.findLaboratoryById(user.getLaboratoryId());
            if (lab == null) {
                throw new IllegalStateException("Laboratory not found.");
            }
            newUserEntity.setLaboratory(lab);
            // Set default parameters for the new user
            newUserEntity.setDeleted(false);
            newUserEntity.setConfirmed(false);
            newUserEntity.setPrivate(true);
            // Create a confirmation token
            String confirmationToken = generateNewToken();
            newUserEntity.setConfirmationToken(confirmationToken);
            newUserEntity.setConfirmationTokenTimestamp(Instant.now());
            // Persist the new User
            userDao.persist(newUserEntity);
            // Send confirmation token by email
            emailService.sendConfirmationEmail(user.getEmail(), confirmationToken);
        } catch (NoResultException e) {
            LOGGER.error(InetAddress.getLocalHost().getHostAddress() + " - Error while persisting user at: " + e.getMessage());
        }
    }

    public void confirmUser(String token) throws UserConfirmationException {
        try {
            UserEntity userEntity = userDao.findUserByConfirmationToken(token);
            if (userEntity != null) {
                userEntity.setConfirmed(true);
                userEntity.setConfirmationToken(null);
                userEntity.setConfirmationTokenTimestamp(null);
            } else {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to confirm user with invalid token");
                throw new UserConfirmationException("Invalid token");
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    public void requestPasswordReset(RequestResetPasswordDto requestResetPasswordDto) throws InvalidPasswordRequestException {
        try {
            UserEntity user = userDao.findUserByEmail(requestResetPasswordDto.getEmail());
            if (user == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with invalid credentials!");
                throw new InvalidPasswordRequestException("Invalid credentials");
            }
            if (isResetTokenNotExpired(user)) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with token not expired at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Request password reset done, please check your email or contact the system administrator");
            }
            String resetToken = generateNewToken();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES));
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    private boolean isResetTokenNotExpired(UserEntity user) {
        return user.getResetPasswordTimestamp() != null && user.getResetPasswordTimestamp().isAfter(Instant.now());
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) throws InvalidPasswordRequestException {
        try {
            UserEntity user = userDao.findUserByResetPasswordToken(resetPasswordDto.getResetToken());
            if (user == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with invalid token at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Invalid token");
            }
            if (isTokenExpired(user)) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with expired token at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Token expired");
            }
            String encryptedPassword = passEncoder.encode(resetPasswordDto.getNewPassword());
            user.setPassword(encryptedPassword);
            user.setResetPasswordToken(null);
            user.setResetPasswordTimestamp(null);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    private boolean isTokenExpired(UserEntity user) {
        return user.getResetPasswordTimestamp().isBefore(Instant.now());
    }


    public Response login(LoginDto userLogin) throws InvalidCredentialsException {
        UserEntity userEntity = userDao.findUserByEmail(userLogin.getEmail());
        if (userEntity == null || !passEncoder.matches(userLogin.getPassword(), userEntity.getPassword())) {
            LOGGER.warn("Failed login attempt for email: " + userLogin.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        String tokenValue = generateNewToken();
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setSessionToken(tokenValue);
        sessionEntity.setUser(userEntity);
        sessionEntity.setLastActivityTimestamp(Instant.now());
        sessionDao.persist(sessionEntity);
        // Create and set the cookie for the token
        NewCookie cookie = new NewCookie("authToken", tokenValue, "/", null, "Auth Token", 3600, false);
        return Response.ok().cookie(cookie).build();
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }


    public void logout(String token) throws InvalidCredentialsException {
        if (token == null) {
            throw new InvalidCredentialsException("No session token found in the request");
        }
        SessionEntity session = sessionDao.findSessionByToken(token);
        if (session != null) {
            sessionDao.remove(session);
            LOGGER.info("User logged out due to invalid or expired token: " + token);
        }
    }

    public boolean tokenValidator(String token) throws InvalidCredentialsException {
        SessionEntity sessionEntity = sessionDao.findSessionByToken(token);
        if (sessionEntity == null) {
            LOGGER.warn("Invalid token: " + token);
            return false;
        }
        int tokenTimerInSeconds = configDao.findConfigValueByKey("sessionTimeout");
        Instant tokenExpiration = sessionEntity.getLastActivityTimestamp().plusSeconds(tokenTimerInSeconds);
        Instant now = Instant.now();
        if (now.isBefore(tokenExpiration)) {
            // Extend the validity of the session by updating the last activity timestamp and Log token validation success
            sessionEntity.setLastActivityTimestamp(now);
            LOGGER.info("Token validation successful for token: " + token);
            return true;
        } else {
            LOGGER.warn("Token expired: " + token);
            logout(token);
            return false;
        }
    }

    public List<UserDto> getAllRegUsers() {
        try {
            ArrayList<UserEntity> users = userDao.findAllUsers();
            if (users != null && !users.isEmpty()) {
                return convertUserEntityListToUserDtoList(users);
            } else {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - No users found at: " + LocalDate.now());
                return Collections.emptyList(); // Return empty list when no users found
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }


    public void updateUserProfile(@Context SecurityContext securityContext, UpdateUserDto updatedUser) throws UserNotFoundException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserByUsername(authUserDto.getName());
        if (userEntity == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update user with invalid token at: ");
            throw new UserNotFoundException("User not found");
        }
        if (updatedUser.getFirstName() != null) userEntity.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) userEntity.setLastName(updatedUser.getLastName());
        if (updatedUser.getPhoto() != null) userEntity.setPhoto(updatedUser.getPhoto());
        if (updatedUser.getBiography() != null) userEntity.setBiography(updatedUser.getBiography());
        LaboratoryEntity laboratory = labDao.findLaboratoryById(updatedUser.getLaboratoryId());
        if (laboratory != null) {
            userEntity.setLaboratory(laboratory);
        }
        userEntity.setPrivate(updatedUser.isPrivate());
        userDao.merge(userEntity);
    }

    public AuthUserDto getAuthUserDtoByToken(String token) throws UserNotFoundException {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            AuthUserDto authUserDto = new AuthUserDto();
            authUserDto.setUsername(userEntity.getUsername());
            authUserDto.setSessionToken(userEntity.getUsername());
            authUserDto.setRoleId(userEntity.getRole().getId());
            return authUserDto;
        } else {
            throw new UserNotFoundException("No user found for this token");
        }
    }

    public UserBasicInfoDto getUserBasicInfo(UserDto user) {
        UserBasicInfoDto userBasicInfo = new UserBasicInfoDto();
        userBasicInfo.setUsername(user.getUsername());
        userBasicInfo.setPhoto(user.getPhoto());
        userBasicInfo.setRole(user.getRoleId());
        return userBasicInfo;
    }

    public ProfileDto getProfileDto(String username, @Context SecurityContext securityContext) throws UserNotFoundException, UnauthorizedAccessException {
        UserDto user = (UserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new UserNotFoundException("No user found for this username");
        }
        if (userEntity.isPrivate() && !user.getUsername().equals(username)) {
            throw new UnauthorizedAccessException("User is private");
        }
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstName(userEntity.getFirstName());
        profileDto.setLastName(userEntity.getLastName());
        profileDto.setPhoto(userEntity.getPhoto());
        profileDto.setBiography(userEntity.getBiography());
        profileDto.setLaboratoryId(userEntity.getLaboratory().getId());
        profileDto.setPrivate(userEntity.isPrivate());
        profileDto.setEmail(userEntity.getEmail());
        return profileDto;
    }

    public void updatePassword(UpdatePasswordDto updatePasswordDto, @Context SecurityContext securityContext) throws InvalidPasswordRequestException, UnknownHostException {
        UserDto user = (UserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserByEmail(user.getEmail());
        if (userEntity == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update user with invalid token");
            throw new InvalidPasswordRequestException("User not found");
        }
        if (!oldPasswordConfirmation(userEntity, updatePasswordDto)) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to update password with invalid old password or password should not be the same at: " + LocalDate.now());
            throw new InvalidPasswordRequestException("Invalid old password or password should not be the same");
        }
        String encryptedNewPassword = passEncoder.encode(updatePasswordDto.getNewPassword());
        userEntity.setPassword(encryptedNewPassword);
    }

    public boolean oldPasswordConfirmation(UserEntity userEntity, UpdatePasswordDto updatePasswordDto) {
        String oldPassword = updatePasswordDto.getOldPassword();
        String newPassword = updatePasswordDto.getNewPassword();
        String hashedPassword = userEntity.getPassword();
        // Checks that the old password provided matches the hashed password and that the new password is different from the one saved
        return passEncoder.matches(oldPassword, hashedPassword) && !passEncoder.matches(newPassword, hashedPassword);
    }

    public void updateRole(UpdateRoleDto updateRoleDto) throws InvalidCredentialsException, UnknownHostException {
        UserEntity u = userDao.findUserByUsername(updateRoleDto.getUsername());
        if (u == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " User not found for this username");
            throw new InvalidCredentialsException("User not found with this username");
        }
        RoleEntity newRole = roleDao.findRoleById(updateRoleDto.getRoleId());
        u.setRole(newRole);
    }

    public boolean isMethodAssociatedWithRole(long roleId, MethodEnum method) throws InvalidCredentialsException, UnknownHostException {
        // Check if user's role has permission to the method
        boolean isMethodAssociated = roleDao.isMethodAssociatedWithRole(roleId, method);
        if (!isMethodAssociated) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Unauthorized method access attempt");
            throw new InvalidCredentialsException("Unauthorized access");
        }
        return true;
    }

    private UserEntity convertUserDtotoUserEntity(UserDto user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(user.getEmail());
        userEntity.setUsername(user.getUsername());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setPhoto(user.getPhoto());
        userEntity.setBiography(user.getBiography());
        return userEntity;
    }

    private UserDto convertUserEntitytoUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setEmail(userEntity.getEmail());
        userDto.setUsername(userEntity.getUsername());
        userDto.setFirstName(userEntity.getFirstName());
        userDto.setLastName(userEntity.getLastName());
        userDto.setPhoto(userEntity.getPhoto());
        userDto.setBiography(userEntity.getBiography());
        userDto.setDeleted(userEntity.isDeleted());
        userDto.setPrivate(userEntity.isPrivate());
        userDto.setConfirmed(userEntity.isConfirmed());
        userDto.setRoleId(userEntity.getRole().getId());
        return userDto;
    }

    private ArrayList<UserDto> convertUserEntityListToUserDtoList(ArrayList<UserEntity> userEntities) {
        ArrayList<UserDto> userDtos = new ArrayList<>();
        for (UserEntity u : userEntities) {
            UserDto userDto = convertUserEntitytoUserDto(u);
            userDtos.add(userDto);
        }
        return userDtos;
    }

}
