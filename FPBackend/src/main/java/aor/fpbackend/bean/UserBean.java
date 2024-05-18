package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.InvalidPasswordRequestException;
import aor.fpbackend.exception.UserConfirmationException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
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


    public void register(UserDto user) throws InvalidCredentialsException, UnknownHostException {
        if ((user == null) || (userDao.checkEmailExist(user.getEmail()))) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with invalid credentials!");
            throw new InvalidCredentialsException("Invalid credentials");
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

    public void requestPasswordReset(ResetPasswordDto resetPasswordDto) throws InvalidPasswordRequestException {
        try {
            UserEntity user = userDao.findUserByEmail(resetPasswordDto.getEmail());
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

    public Response login(LoginDto userLogin, HttpServletResponse response) throws InvalidCredentialsException {
        try {
            UserEntity userEntity = userDao.findUserByEmail(userLogin.getEmail());
            if (userEntity != null) {
                // Retrieve the hashed password associated with the user
                String hashedPassword = userEntity.getPassword();
                // Check if the provided password matches the hashed password
                if (passEncoder.matches(userLogin.getPassword(), hashedPassword)) {
                    String tokenValue = generateNewToken();
                    SessionEntity sessionEntity = new SessionEntity();
                    sessionEntity.setSessionToken(tokenValue);
                    sessionEntity.setUser(userEntity);
                    sessionEntity.setLastActivityTimestamp(Instant.now());
                    sessionDao.persist(sessionEntity);
                    // Create and set the cookie for the token
                    NewCookie cookie = new NewCookie("authToken", tokenValue, "/", null, "Auth Token", 3600, false);
                    return Response.ok()
                            .cookie(cookie)
                            .build();
                } else {
                    LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to login with invalid credentials: " + userLogin.getEmail());
                    throw new InvalidCredentialsException("Invalid credentials");
                }
            } else {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to login with invalid credentials: " + userLogin.getEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public void logout(String token) throws InvalidCredentialsException {
        try {
            SessionEntity session = sessionDao.findSessionByToken(token);
            if (session == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to logout with invalid token at: " + LocalDate.now());
                throw new InvalidCredentialsException("User not found");
            }
            sessionDao.remove(session);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
        }
    }

    public boolean tokenValidator(String token) throws InvalidCredentialsException {
        SessionEntity sessionEntity = sessionDao.findSessionByToken(token);
        if (sessionEntity != null) {
//            int tokenTimerInSeconds = configDao.findConfigValueByKey("sessionTimeout");
//            Instant tokenExpiration = sessionEntity.getLastActivityTimestamp().plusSeconds(tokenTimerInSeconds);
//            Instant now = Instant.now();
//            if (now.isBefore(tokenExpiration)) {
//                sessionEntity.setLastActivityTimestamp(now);
//                return true;
            return true; // Remove the token expiration check
            } else {
                logout(token);
                return false;
            }
        }
//        return false;
//    }

    public UserDto getLoggedUser(String tokenValue) throws UserNotFoundException {
        try {
            UserEntity userEntity = userDao.findUserByToken(tokenValue);

            if (userEntity != null) {
                return convertUserEntitytoUserDto(userEntity);
            } else {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - No users found at: " + LocalDate.now());
                throw new UserNotFoundException("No user found for this token");
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to retrieve host address", e);
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

    public void updateUserProfile(String token, ProfileDto updatedUser) throws UserNotFoundException, UnknownHostException {
        UserEntity userEntity = userDao.findUserByToken(token);
        if(userEntity == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update user with invalid token at: " + token);
            throw new UserNotFoundException("User not found");
        }
        if(updatedUser.getFirstName() != null) userEntity.setFirstName(updatedUser.getFirstName());
        if(updatedUser.getLastName() != null) userEntity.setLastName(updatedUser.getLastName());
        if(updatedUser.getPhoto() != null) userEntity.setPhoto(updatedUser.getPhoto());
        if(updatedUser.getBiography() != null) userEntity.setBiography(updatedUser.getBiography());
        LaboratoryEntity laboratory = labDao.findLaboratoryById(updatedUser.getLaboratoryId());
        if(laboratory != null) {
            userEntity.setLaboratory(laboratory);
        }
        userEntity.setPrivate(updatedUser.isPrivate());
    }
    public UserDto getUserByToken(String token) throws UserNotFoundException {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            return convertUserEntitytoUserDto(userEntity);
        } else {
            throw new UserNotFoundException("No user found for this token");
        }
    }
    public UserBasicInfoDto getUserBasicInfo(UserDto user) {
        UserBasicInfoDto userBasicInfo = new UserBasicInfoDto();
        userBasicInfo.setNickname(user.getNickname());
        userBasicInfo.setPhoto(user.getPhoto());
        System.out.println(userBasicInfo + " -3");

        return userBasicInfo;
    }
    public ProfileDto getProfileDto(String nickname) throws UserNotFoundException {
        UserEntity userEntity = userDao.findUserByNickname(nickname);
        if (userEntity != null) {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setFirstName(userEntity.getFirstName());
            profileDto.setLastName(userEntity.getLastName());
            profileDto.setPhoto(userEntity.getPhoto());
            profileDto.setBiography(userEntity.getBiography());
            profileDto.setLaboratoryId(userEntity.getLaboratory().getId());
            profileDto.setPrivate(userEntity.isPrivate());
            profileDto.setEmail(userEntity.getEmail());
            return profileDto;
        } else {
            throw new UserNotFoundException("No user found for this nickname");
        }
    }

    private UserEntity convertUserDtotoUserEntity(UserDto user) {
        UserEntity userEntity = new UserEntity();
        if (userEntity != null) {
            userEntity.setEmail(user.getEmail());
            userEntity.setNickname(user.getNickname());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setPhoto(user.getPhoto());
            userEntity.setBiography(user.getBiography());
            return userEntity;
        }
        return null;
    }

    private UserDto convertUserEntitytoUserDto(UserEntity userEntity) {
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setEmail(userEntity.getEmail());
        userDto.setNickname(userEntity.getNickname());
        userDto.setFirstName(userEntity.getFirstName());
        userDto.setLastName(userEntity.getLastName());
        userDto.setPhoto(userEntity.getPhoto());
        userDto.setBiography(userEntity.getBiography());
        userDto.setDeleted(userEntity.isDeleted());
        userDto.setPrivate(userEntity.isPrivate());
        userDto.setConfirmed(userEntity.isConfirmed());
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
