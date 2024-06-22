package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import aor.fpbackend.utils.JwtKeyProvider;
import io.jsonwebtoken.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import aor.fpbackend.exception.UserNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(UserBean.class);

    @EJB
    PassEncoder passEncoder;
    @EJB
    EmailService emailService;
    @EJB
    UserDao userDao;
    @EJB
    SessionDao sessionDao;
    @EJB
    RoleDao roleDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    ConfigurationBean configurationBean;
    @EJB
    SessionBean sessionBean;


    public void createDefaultUserIfNotExistent(String username, String photo, long roleId, long labId) throws DatabaseOperationException {
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
            UserEntity userEntity = new UserEntity(email, encryptedPassword, username, username, username, photo, true, false, true, laboratory, role, Instant.now());
            userDao.persist(userEntity);
        }
    }

    public void register(UserRegisterDto user) throws InvalidCredentialsException, UnknownHostException {
        if (user == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with invalid credentials!");
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (userDao.checkEmailExist(user.getEmail()) || userDao.checkUsernameExist(user.getUsername())) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with invalid credentials!");
            throw new InvalidCredentialsException("Invalid credentials - Email or username already exists");
        }

        try {
            UserEntity newUserEntity = convertUserRegisterDtotoUserEntity(user);
            String encryptedPassword = passEncoder.encode(user.getPassword());
            newUserEntity.setPassword(encryptedPassword);
            // Retrieve and set the default role "Standard User"
            RoleEntity role = roleDao.findRoleByName(UserRoleEnum.STANDARD_USER);
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
            // TODO Verificar a fotografia Default
            newUserEntity.setPhoto("https://www.silcharmunicipality.in/wp-content/uploads/2021/02/male-face.jpg");
            // Create a confirmation token
            String confirmationToken = sessionBean.generateNewToken();
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

    public void requestPasswordReset(PasswordRequestResetDto passwordRequestResetDto) throws InvalidPasswordRequestException {
        try {
            UserEntity user = userDao.findUserByEmail(passwordRequestResetDto.getEmail());
            if (user == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with invalid credentials!");
                throw new InvalidPasswordRequestException("Invalid credentials");
            }
            if (isResetTokenNotExpired(user)) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with token not expired at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Request password reset done, please check your email or contact the system administrator");
            }
            String resetToken = sessionBean.generateNewToken();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES));
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    public void requestNewConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
        UserEntity user = userDao.findUserByEmail(email.getEmail());
        if (user == null) {
            LOGGER.warn("Attempt to request new confirmation email at - User not foud: " + email.getEmail());
            throw new InvalidRequestOnRegistConfirmationException("Not found user with this email");
        }
        if (user.isConfirmed()) {
            LOGGER.warn("Attempt to request new confirmation email at - User already confirmed: " + email.getEmail());
            throw new InvalidRequestOnRegistConfirmationException("Not possible to request confirmation email please contact the administrator");
        }
        if (user.getLastSentEmailTimestamp() != null) {
            Instant now = Instant.now();
            Instant lastSentEmail = user.getLastSentEmailTimestamp();
            long timeDifference = ChronoUnit.MINUTES.between(lastSentEmail, now);
            if (timeDifference < 1) {
                LOGGER.warn("Attempt to request new confirmation email at - Time difference less than 1 minute" +
                        LocalDateTime.now() + ": " + email.getEmail());
                throw new InvalidRequestOnRegistConfirmationException("You can't request a new confirmation email now, please wait 1 minute");
            }
        }
        emailService.sendConfirmationEmail(user.getEmail(), user.getConfirmationToken());
        user.setLastSentEmailTimestamp(Instant.now());
    }

    private boolean isResetTokenNotExpired(UserEntity user) {
        return user.getResetPasswordTimestamp() != null && user.getResetPasswordTimestamp().isAfter(Instant.now());
    }

    public void resetPassword(PasswordResetDto passwordResetDto) throws InvalidPasswordRequestException {
        try {
            UserEntity user = userDao.findUserByResetPasswordToken(passwordResetDto.getResetToken());
            if (user == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with invalid token at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Invalid token");
            }
            if (!isResetTokenNotExpired(user)) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to reset password with expired token at: " + LocalDate.now());
                throw new InvalidPasswordRequestException("Token expired");
            }
            String encryptedPassword = passEncoder.encode(passwordResetDto.getNewPassword());
            user.setPassword(encryptedPassword);
            user.setResetPasswordToken(null);
            user.setResetPasswordTimestamp(null);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    public Response login(UserLoginDto userLogin) throws InvalidCredentialsException {
        UserEntity userEntity = userDao.findUserByEmail(userLogin.getEmail());
        if (userEntity == null || !passEncoder.matches(userLogin.getPassword(), userEntity.getPassword())) {
            LOGGER.warn("Failed login attempt for email: " + userLogin.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        int definedTimeOut = configurationBean.getConfigValueByKey("sessionTimeout");
        Instant now = Instant.now();
        // Calcular o Instant de expiração adicionando o tempo de expiração em milissegundos
        Instant expirationInstant = now.plus(Duration.ofMillis(definedTimeOut));
        String authToken = sessionBean.generateJwtToken(userEntity, definedTimeOut, "auth");
        NewCookie authCookie = new NewCookie("authToken", authToken, "/", null, "Auth Token", 3600, false, true);
        String sessionToken = sessionBean.generateJwtToken(userEntity, definedTimeOut, "session");
        NewCookie sessionCookie = new NewCookie("sessionToken", sessionToken, "/", null, "Session Token", 3600, false, false);
        sessionDao.persist(new SessionEntity(authToken, sessionToken, expirationInstant, userEntity));
        return Response.ok().cookie(authCookie).cookie(sessionCookie).build();
    }


    public void logout(SecurityContext securityContext) throws InvalidCredentialsException, UnknownHostException {
        // Invalida a sessão antiga
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        sessionDao.inativateSessionbyAuthToken(authUserDto.getToken());
        // Configura cookies para expirar imediatamente
    }

    public List<UsernameDto> getAllRegUsers() {
        try {
            ArrayList<UserEntity> userEntities = userDao.findAllUsers();
            if (userEntities != null && !userEntities.isEmpty()) {
                ArrayList<UsernameDto> usernameDtos = new ArrayList<>();
                for (UserEntity u : userEntities) {
                    UsernameDto usernameDto = new UsernameDto();
                    usernameDto.setId(u.getId());
                    usernameDto.setUsername(u.getUsername());
                    usernameDtos.add(usernameDto);
                }
                return usernameDtos;
            } else {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - No users found at: " + LocalDate.now());
                return Collections.emptyList(); // Return empty list when no users found
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    public void updateUserProfile(@Context SecurityContext securityContext, UserUpdateDto updatedUser) throws UserNotFoundException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
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

    public UserBasicInfoDto getUserBasicInfo(@Context SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        return convertUserEntitytoUserBasicInfoDto(userEntity);
    }

    public List<UserBasicInfoDto> getUsersBasicInfoByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<UserEntity> userEntities = userDao.getUsersByFirstLetter(lowerCaseFirstLetter);
        return convertUserEntityListToUserBasicInfoDtoList(userEntities);
    }

    public UserProfileDto getProfileDto(String username, @Context SecurityContext securityContext) throws UserNotFoundException, UnauthorizedAccessException, ForbiddenAccessException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new UserNotFoundException("No user found for this username");
        }
        if (userEntity.isPrivate() && !authUserDto.getUserId().equals(userEntity.getId())) {
            throw new ForbiddenAccessException("User is private");
        }
        return convertUserEntitytoUserProfileDto(userEntity);
    }

    public void updatePassword(PasswordUpdateDto passwordUpdateDto, @Context SecurityContext securityContext) throws InvalidPasswordRequestException, UnknownHostException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (!oldPasswordConfirmation(userEntity, passwordUpdateDto)) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to update password with invalid old password or password should not be the same at: " + LocalDate.now());
            throw new InvalidPasswordRequestException("Invalid old password or password should not be the same");
        }
        String encryptedNewPassword = passEncoder.encode(passwordUpdateDto.getNewPassword());
        userEntity.setPassword(encryptedNewPassword);
    }

    public boolean oldPasswordConfirmation(UserEntity userEntity, PasswordUpdateDto passwordUpdateDto) {
        String oldPassword = passwordUpdateDto.getOldPassword();
        String newPassword = passwordUpdateDto.getNewPassword();
        String hashedPassword = userEntity.getPassword();
        // Checks that the old password provided matches the hashed password and that the new password is different from the one saved
        return passEncoder.matches(oldPassword, hashedPassword) && !passEncoder.matches(newPassword, hashedPassword);
    }

    public void updateRole(UserUpdateRoleDto userUpdateRoleDto) throws InvalidCredentialsException, UnknownHostException {
        UserEntity u = userDao.findUserByUsername(userUpdateRoleDto.getUsername());
        if (u == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " User not found for this username");
            throw new InvalidCredentialsException("User not found with this username");
        }
        RoleEntity newRole = roleDao.findRoleById(userUpdateRoleDto.getRoleId());
        u.setRole(newRole);
    }

    public UserBasicInfoDto getUserBasicInfoById(long userId) {
        UserEntity userEntity = userDao.findUserById(userId);
        return convertUserEntitytoUserBasicInfoDto(userEntity);
    }

    public List<ProjectMembershipDto> getUsersByProject(long projectId) {
        return userDao.getUsersByProject(projectId);
    }

    public UserEntity convertUserRegisterDtotoUserEntity(UserRegisterDto user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(user.getEmail());
        userEntity.setUsername(user.getUsername());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        return userEntity;
    }

    public UserProfileDto convertUserEntitytoUserProfileDto(UserEntity userEntity) {
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setId(userEntity.getId());
        userProfileDto.setEmail(userEntity.getEmail());
        userProfileDto.setUsername(userEntity.getUsername());
        userProfileDto.setFirstName(userEntity.getFirstName());
        userProfileDto.setLastName(userEntity.getLastName());
        userProfileDto.setPhoto(userEntity.getPhoto());
        userProfileDto.setBiography(userEntity.getBiography());
        userProfileDto.setLaboratoryId(userEntity.getLaboratory().getId());
        userProfileDto.setPrivate(userEntity.isPrivate());
        return userProfileDto;
    }

    public UserBasicInfoDto convertUserEntitytoUserBasicInfoDto(UserEntity userEntity) {
        UserBasicInfoDto userBasicInfo = new UserBasicInfoDto();
        userBasicInfo.setUsername(userEntity.getUsername());
        userBasicInfo.setRole(userEntity.getRole().getId());
        userBasicInfo.setPhoto(userEntity.getPhoto());
        userBasicInfo.setId(userEntity.getId());
        return userBasicInfo;
    }

    public List<UserBasicInfoDto> convertUserEntityListToUserBasicInfoDtoList(List<UserEntity> userEntities) {
        ArrayList<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (UserEntity u : userEntities) {
            UserBasicInfoDto userBasicInfoDto = convertUserEntitytoUserBasicInfoDto(u);
            userBasicInfoDtos.add(userBasicInfoDto);
        }
        return userBasicInfoDtos;
    }

    public UserBasicInfoDto convertUserEntetyToUserBasicInfoDto(UserEntity userEntity) {
        UserBasicInfoDto userBasicInfoDto = new UserBasicInfoDto();
        userBasicInfoDto.setId(userEntity.getId());
        userBasicInfoDto.setUsername(userEntity.getUsername());
        userBasicInfoDto.setRole(userEntity.getRole().getId());
        userBasicInfoDto.setPhoto(userEntity.getPhoto());
        return userBasicInfoDto;
    }
}
