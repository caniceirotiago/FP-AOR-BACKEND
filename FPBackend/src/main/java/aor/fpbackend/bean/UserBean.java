package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Email.EmailDto;
import aor.fpbackend.dto.Password.PasswordRequestResetDto;
import aor.fpbackend.dto.Password.PasswordResetDto;
import aor.fpbackend.dto.Password.PasswordUpdateDto;
import aor.fpbackend.dto.Project.ProjectMembershipDto;
import aor.fpbackend.dto.User.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import aor.fpbackend.utils.GlobalSettings;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.*;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

import aor.fpbackend.exception.UserNotFoundException;
import org.apache.logging.log4j.ThreadContext;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * UserBean is a stateless EJB that provides services for user management, including registration,
 * authentication, profile updates, and password management.
 * <p>
 * This bean handles the core logic for user-related operations, interacting with various DAO
 * and utility classes to perform tasks such as encoding passwords, sending emails, and managing sessions.
 * It ensures the integrity and security of user data by performing necessary checks and validations
 * before persisting or updating any information.
 * <p>
 * Dependencies are injected using the @EJB annotation, which includes DAOs for user, session, role,
 * and laboratory entities, as well as utility classes for password encoding, email services,
 * and session management.
 */


@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(UserBean.class);


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

    /**
     * Registers a new user in the system.
     * <p>
     * This method performs several tasks to register a user:
     * <ul>
     *     <li>Checks if the email or username already exists.</li>
     *     <li>Encodes the user's password.</li>
     *     <li>Assigns the default role and the specified laboratory to the user.</li>
     *     <li>Sets default parameters for the new user.</li>
     *     <li>Generates a confirmation token and sends a confirmation email.</li>
     *     <li>Persists the new user entity in the database.</li>
     * </ul>
     * </p>
     *
     * @param userRgDto the data transfer object containing user registration details.
     * @throws InvalidCredentialsException if the email or username already exists, or if there is an error during user persistence.
     * @throws EntityNotFoundException if the default role or laboratory is not found.
     */
    @Transactional
    public void register(UserRegisterDto userRgDto) throws InvalidCredentialsException, EntityNotFoundException {
        if (userDao.checkEmailAndUsernameExist(userRgDto.getEmail(), userRgDto.getUsername()))  {
            throw new InvalidCredentialsException("Attempt to register with invalid credentials - Email or username already exists, Email: " + userRgDto.getEmail() + " Username: " + userRgDto.getUsername());
        }
        RoleEntity role = roleDao.findRoleByName(UserRoleEnum.STANDARD_USER);
        if (role == null) {
            throw new EntityNotFoundException("Default role not found.");
        }
        LaboratoryEntity lab = labDao.findLaboratoryById(userRgDto.getLaboratoryId());
        if (lab == null) {
            throw new EntityNotFoundException("Laboratory not found.");
        }
        try {
            UserEntity newUserEntity = convertUserRegisterDtotoUserEntity(userRgDto);
            newUserEntity.setRole(role);
            newUserEntity.setPassword(passEncoder.encode(userRgDto.getPassword()));
            newUserEntity.setLaboratory(lab);
            newUserEntity.setDeleted(false);
            newUserEntity.setConfirmed(false);
            newUserEntity.setPrivate(true);
            newUserEntity.setPhoto(GlobalSettings.USER_DEFAULT_PHOTO_URL);
            // Create a confirmation token
            String confirmationToken = sessionBean.generateNewToken();
            newUserEntity.setConfirmationToken(confirmationToken);
            newUserEntity.setConfirmationTokenTimestamp(Instant.now());

            userDao.persist(newUserEntity);
            emailService.sendConfirmationEmail(userRgDto.getEmail(), confirmationToken);
            LOGGER.info("Registration successful for the username: " + newUserEntity.getUsername() + " and email: " + newUserEntity.getEmail());
        } catch (NoResultException e) {
            LOGGER.error("Error while persisting user at: " + e.getMessage() + "for email: " + userRgDto.getEmail() + " and username: " + userRgDto.getUsername());
            throw new InvalidCredentialsException("Error while persisting user");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Confirms a user's registration using the provided confirmation token.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the provided token to ensure it is not null or empty.</li>
     *     <li>Retrieves the user associated with the confirmation token.</li>
     *     <li>Confirms the user's registration by setting the confirmation status to true and clearing the confirmation token and timestamp.</li>
     *     <li>Logs the operation and clears the ThreadContext to ensure no residual data remains.</li>
     * </ul>
     * </p>
     *
     * @param token the confirmation token used to validate and confirm the user's registration.
     * @throws InputValidationException if the provided token is null or empty.
     * @throws UserNotFoundException if no user is found for the provided confirmation token.
     */

    @Transactional
    public void confirmUser(String token) throws InputValidationException, UserNotFoundException {
        if(token == null || token.isEmpty()){
            LOGGER.warn("Attempt to confirm user with empty token");
            throw new InputValidationException("Invalid token");
        }
        UserEntity userEntity = userDao.findUserByConfirmationToken(token);
        if (userEntity == null) {
            throw new UserNotFoundException("Attempt to confirm user with invalid token");
        }
        if (userEntity.getConfirmationTokenTimestamp().isBefore(Instant.now().minus(GlobalSettings.CONFIRMATION_TOKEN_EXPIRATION_TIME_H, ChronoUnit.HOURS))) {
            LOGGER.warn("Attempt to confirm user with expired token");
            throw new InputValidationException("Token expired - expiration time is " + GlobalSettings.CONFIRMATION_TOKEN_EXPIRATION_TIME_H + " hours");
        }
        ThreadContext.put("userId", String.valueOf(userEntity.getId()));
        ThreadContext.put("username", userEntity.getUsername());
        try {
            userEntity.setConfirmed(true);
            userEntity.setConfirmationToken(null);
            userEntity.setConfirmationTokenTimestamp(null);
            LOGGER.info("User confirmed successfully");
        } catch (NoResultException e) {
            LOGGER.error("Error while confirming user at: " + e.getMessage());
            throw new UserNotFoundException("Error while confirming user");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Requests a password reset for a user identified by the provided email.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the email to ensure the user exists.</li>
     *     <li>Checks if a password reset request was made recently.</li>
     *     <li>Generates a new password reset token and sets a timestamp to prevent immediate subsequent requests.</li>
     *     <li>Sends a password reset email to the user with the reset token.</li>
     *     <li>Logs the operation and clears the ThreadContext to ensure no residual data remains.</li>
     * </ul>
     * </p>
     *
     * @param passwordRequestResetDto the data transfer object containing the user's email for the password reset request.
     * @throws UserNotFoundException if no user is found for the provided email.
     * @throws IllegalStateException if a password reset request has been made recently.
     */
    @Transactional
    public void requestPasswordReset(PasswordRequestResetDto passwordRequestResetDto) throws UserNotFoundException, ForbiddenAccessException {
        UserEntity user = userDao.findUserByEmail(passwordRequestResetDto.getEmail());
        if (user == null) {
            throw new UserNotFoundException("Attempt to reset password with invalid credentials!");
        }
        if (user.getResetPasswordTimestamp() != null && user.getResetPasswordTimestamp().isAfter(Instant.now())) {
            throw new ForbiddenAccessException("You can only make a password reset request every " + GlobalSettings.PASSWORD_RESET_PREVENT_TIMER_MIN + " minutes");
        }
        try {
            ThreadContext.put("username", user.getUsername());
            ThreadContext.put("userId", String.valueOf(user.getId()));

            String resetToken = sessionBean.generateNewToken();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTimestamp(Instant.now().plus(GlobalSettings.PASSWORD_RESET_PREVENT_TIMER_MIN, ChronoUnit.MINUTES));
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            LOGGER.info("Request password reset done for email: " + user.getEmail());

        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Service responsible for requesting the resend of a registration confirmation email.
     * This method checks if the user exists, if they have already been confirmed, and if they are not
     * requesting confirmation emails too frequently before sending a new confirmation email.
     *
     * @param email the DTO containing the email of the user requesting the resend of the confirmation email.
     * @throws InvalidRequestOnRegistConfirmationException if the user is not found,
     *         has already been confirmed, or is requesting confirmation emails
     *         at intervals shorter than allowed.
     * @throws UnknownHostException if there is an error resolving the host when sending the email.
     */
    @Transactional
    public void requestNewConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException, UnknownHostException {
        UserEntity user = userDao.findUserByEmail(email.getEmail());
        if (user == null) {
            throw new InvalidRequestOnRegistConfirmationException("Attempt to request new confirmation email with user not found");
        }
        if (user.isConfirmed()) {
            throw new InvalidRequestOnRegistConfirmationException("Attempt to request new confirmation email with user already confirmed");
        }
        if (user.getLastSentEmailTimestamp() != null) {
            Instant now = Instant.now();
            Instant lastSentEmail = user.getLastSentEmailTimestamp();
            long timeDifference = ChronoUnit.MINUTES.between(lastSentEmail, now);
            if (timeDifference < GlobalSettings.CONFIRMATION_EMAIL_PREVENT_TIMER_MIN) {
                throw new InvalidRequestOnRegistConfirmationException("You can't request a new confirmation email now, please wait " + (GlobalSettings.CONFIRMATION_EMAIL_PREVENT_TIMER_MIN - timeDifference) + " minute/s");
            }
        }
        try{
            ThreadContext.put("username", user.getUsername());
            ThreadContext.put("userId", String.valueOf(user.getId()));

            emailService.sendConfirmationEmail(user.getEmail(), user.getConfirmationToken());
            user.setLastSentEmailTimestamp(Instant.now());
            LOGGER.info("Request new confirmation email");
        } finally {
            ThreadContext.clearMap();
        }
    }

    private boolean isResetTokenNotExpired(UserEntity user) {
        return user.getResetPasswordTimestamp() != null && user.getResetPasswordTimestamp().isAfter(Instant.now());
    }

    public void resetPassword(PasswordResetDto passwordResetDto) throws IllegalStateException {
        try {
            UserEntity user = userDao.findUserByResetPasswordToken(passwordResetDto.getResetToken());
            if (user == null) {
                LOGGER.warn("Attempt to reset password with invalid token");
                throw new IllegalStateException("Invalid token");
            }
            if (!isResetTokenNotExpired(user)) {
                LOGGER.warn("Attempt to reset password with expired token");
                throw new IllegalStateException("Token expired");
            }
            user.setPassword(passEncoder.encode(passwordResetDto.getNewPassword()));
            user.setResetPasswordToken(null);
            user.setResetPasswordTimestamp(null);
            LOGGER.info("Reset password");
        } finally {
            ThreadContext.clearMap();
        }
    }

    public Response login(UserLoginDto userLogin) throws InvalidCredentialsException {
        try{
        UserEntity userEntity = userDao.findUserByEmail(userLogin.getEmail());
        if (userEntity == null || !passEncoder.matches(userLogin.getPassword(), userEntity.getPassword())) {
            LOGGER.warn("Failed login attempt");
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

        LOGGER.info(" Successful login for username: " + userEntity.getUsername() + " with user id: " + userEntity.getId());
        ThreadContext.clearMap();

        return Response.ok().cookie(authCookie).cookie(sessionCookie).build();
        } finally {
            ThreadContext.clearMap();
        }
    }

    public void logout(SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        sessionDao.inativateSessionbyAuthToken(authUserDto.getToken());
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
                LOGGER.warn("No users found");
                return Collections.emptyList(); // Return empty list when no users found
            }
        } finally {
            ThreadContext.clearMap();
        }
    }

    public void updateUserProfile(@Context SecurityContext securityContext, UserUpdateDto updatedUser) throws UserNotFoundException, UnknownHostException {
        try {
            AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
            UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
            if (userEntity == null) {
                LOGGER.warn("Attempt to update user with invalid token");
                throw new UserNotFoundException("User not found");
            }
            ThreadContext.put("author", userEntity.getUsername());
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
            LOGGER.info("User profile updated");
        } finally {
            ThreadContext.clearMap();
        }
    }

    public UserBasicInfoDto getUserBasicInfo(@Context SecurityContext securityContext) throws UserNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        return convertUserEntitytoUserBasicInfoDto(userEntity);
    }

    public List<UserBasicInfoDto> getUsersListBasicInfo() {
        return convertUserEntityListToUserBasicInfoDtoList(userDao.findAllUsers());
    }

    public List<UserBasicInfoDto> getUsersBasicInfoByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<UserEntity> userEntities = userDao.getUsersByFirstLetter(lowerCaseFirstLetter);
        return convertUserEntityListToUserBasicInfoDtoList(userEntities);
    }

    public UserProfileDto getProfileDto(String username, @Context SecurityContext securityContext) throws UserNotFoundException, ForbiddenAccessException {
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

    public void updatePassword(PasswordUpdateDto passwordUpdateDto, @Context SecurityContext securityContext) throws IllegalStateException, UnknownHostException, UserNotFoundException {
        ThreadContext.put("ip", InetAddress.getLocalHost().getHostAddress());
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        ThreadContext.put("author", userEntity.getUsername());
        if (!oldPasswordConfirmation(userEntity, passwordUpdateDto)) {
            LOGGER.warn("Attempt to update password with invalid old password or repeated new password");
            throw new IllegalStateException("Invalid old password or repeated new password");
        }
        String encryptedNewPassword = passEncoder.encode(passwordUpdateDto.getNewPassword());
        userEntity.setPassword(encryptedNewPassword);
        LOGGER.info("Password updated successfully");
        ThreadContext.clearMap();
    }

    private boolean oldPasswordConfirmation(UserEntity userEntity, PasswordUpdateDto passwordUpdateDto) {
        String oldPassword = passwordUpdateDto.getOldPassword();
        String newPassword = passwordUpdateDto.getNewPassword();
        String hashedPassword = userEntity.getPassword();
        // Checks that the old password provided matches the hashed password and that the new password is different from the one saved
        return passEncoder.matches(oldPassword, hashedPassword) && !passEncoder.matches(newPassword, hashedPassword);
    }

    public void updateRole(UserUpdateRoleDto userUpdateRoleDto) throws InvalidCredentialsException, UnknownHostException, EntityNotFoundException {
        ThreadContext.put("ip", InetAddress.getLocalHost().getHostAddress());
        UserEntity userEntity = userDao.findUserById(userUpdateRoleDto.getUserId());
        if (userEntity == null) {
            LOGGER.warn("User not found for this username");
            throw new InvalidCredentialsException("User not found with this username");
        }
        ThreadContext.put("author", userEntity.getUsername());
        RoleEntity newRole = roleDao.findRoleById(userUpdateRoleDto.getRoleId());
        if (newRole == null) {
            throw new EntityNotFoundException("Role not found with this Id");
        }
        userEntity.setRole(newRole);
        LOGGER.info("User Role updated successfully");
        ThreadContext.clearMap();
    }

    public UserBasicInfoDto getUserBasicInfoById(long userId) {
        UserEntity userEntity = userDao.findUserById(userId);
        return convertUserEntitytoUserBasicInfoDto(userEntity);
    }

    public List<ProjectMembershipDto> getUsersByProject(long projectId) {
        return userDao.getUsersByProject(projectId);
    }

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
}
