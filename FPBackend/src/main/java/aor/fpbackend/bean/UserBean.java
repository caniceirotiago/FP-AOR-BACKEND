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
import aor.fpbackend.utils.PassEncoder;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.*;
import org.apache.logging.log4j.LogManager;
import java.io.Serializable;
import java.net.UnknownHostException;
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
 * </p>
 * <p>
 * Technologies Used:
 * <ul>
 *     <li><b>Java EE</b>: For building the EJB and managing transactions.</li>
 *     <li><b>BCrypt</b>: For password hashing and validation via {@link PassEncoder}.</li>
 *     <li><b>JWT</b>: For token generation and validation via {@link SessionBean}.</li>
 *     <li><b>JPA</b>: For database interactions via DAOs.</li>
 *     <li><b>Jakarta EE</b>: For RESTful web services and dependency injection.</li>
 *     <li><b>SLF4J</b>: For logging operations.</li>
 * </ul>
 * </p>
 * <p>
 * Dependencies are injected using the {@link EJB} annotation, which includes DAOs for user, session, role,
 * and laboratory entities, as well as utility classes for password encoding, email services,
 * and session management.
 * </p>
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
    RoleDao roleDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
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
     * <p>
     * This method checks if the user exists, if they have already been confirmed, and if they are not
     * requesting confirmation emails too frequently before sending a new confirmation email.
     * </p>
     * @param email the DTO containing the email of the user requesting the resend of the confirmation email.
     * @throws InvalidRequestOnRegistConfirmationException if the user is not found,
     *         has already been confirmed, or is requesting confirmation emails
     *         at intervals shorter than allowed.
     */
    @Transactional
    public void requestNewConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
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

    /**
     * Service responsible for resetting the user's password.
     * <p>
     * This method validates the reset token, checks if it has not expired,
     * and then updates the user's password. If the token is invalid or expired,
     * appropriate exceptions are thrown.
     * </p>
     * @param passwordResetDto the DTO containing the reset token and the new password.
     * @throws IllegalStateException if the reset token has expired.
     * @throws UserNotFoundException if the user associated with the reset token is not found.
     */
    public void resetPassword(PasswordResetDto passwordResetDto) throws IllegalStateException, UserNotFoundException, ForbiddenAccessException {
        UserEntity user = userDao.findUserByResetPasswordToken(passwordResetDto.getResetToken());
        if (user == null) {
            throw new UserNotFoundException("Attempt to reset password with invalid token");
        }
        if (!(user.getResetPasswordTimestamp() != null && user.getResetPasswordTimestamp().isAfter(Instant.now()))) {
            throw new ForbiddenAccessException("Attempt to reset password with expired token");
        }
        try {
            ThreadContext.put("username", user.getUsername());
            ThreadContext.put("userId", String.valueOf(user.getId()));

            user.setPassword(passEncoder.encode(passwordResetDto.getNewPassword()));
            user.setResetPasswordToken(null);
            user.setResetPasswordTimestamp(null);
            LOGGER.info("Reset password");
        } finally {
            ThreadContext.clearMap();
        }
    }





        //todo: para já comentado.. acho que não está a ser utilziado
//    public List<UsernameDto> getAllRegUsers() {
//        try {
//            ArrayList<UserEntity> userEntities = userDao.findAllUsers();
//            if (userEntities != null && !userEntities.isEmpty()) {
//                ArrayList<UsernameDto> usernameDtos = new ArrayList<>();
//                for (UserEntity u : userEntities) {
//                    UsernameDto usernameDto = new UsernameDto();
//                    usernameDto.setId(u.getId());
//                    usernameDto.setUsername(u.getUsername());
//                    usernameDtos.add(usernameDto);
//                }
//                return usernameDtos;
//            } else {
//                LOGGER.warn("No users found");
//                return Collections.emptyList(); // Return empty list when no users found
//            }
//        } finally {
//            ThreadContext.clearMap();
//        }
//    }

    /**
     * Updates the authenticated user's profile with the provided details.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's entity from the database.</li>
     *     <li>Validates and updates the user's profile details (first name, last name, photo, laboratory, biography, and privacy setting).</li>
     *     <li>Persists the updated user entity in the database.</li>
     *     <li>Logs the profile update operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param securityContext the security context containing the authenticated user's details.
     * @param updatedUser the DTO containing the updated user profile details.
     * @throws UserNotFoundException if the authenticated user is not found.
     * @throws EntityNotFoundException if the specified laboratory is not found.
     * @throws DatabaseOperationException if an error occurs while persisting the updated profile.
     */
    @Transactional
    public void updateUserProfile(@Context SecurityContext securityContext, UserUpdateDto updatedUser) throws UserNotFoundException, EntityNotFoundException, DatabaseOperationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        LaboratoryEntity laboratory = labDao.findLaboratoryById(updatedUser.getLaboratoryId());
        if (laboratory == null) {
            throw new EntityNotFoundException("Laboratory not found");
        }
        try {
            if (updatedUser.getFirstName() != null) userEntity.setFirstName(updatedUser.getFirstName());
            if (updatedUser.getLastName() != null) userEntity.setLastName(updatedUser.getLastName());
            if (updatedUser.getPhoto() != null) userEntity.setPhoto(updatedUser.getPhoto());
            userEntity.setLaboratory(laboratory);
            if (updatedUser.getBiography() != null) userEntity.setBiography(updatedUser.getBiography());
            userEntity.setPrivate(updatedUser.isPrivate());
            userDao.merge(userEntity);
            LOGGER.info("User profile updated");
        } catch (PersistenceException e) {
            throw new DatabaseOperationException("Persistence error while updating user profile, " +  e);
        }finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Retrieves the basic information of the authenticated user.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's entity from the database using their ID.</li>
     *     <li>Converts the user entity to a UserBasicInfoDto object.</li>
     * </ul>
     * </p>
     * @param securityContext the security context containing the authenticated user's details.
     * @return a UserBasicInfoDto object representing the authenticated user's basic information.
     * @throws UserNotFoundException if the authenticated user is not found.
     */
    public UserBasicInfoDto getUserBasicInfo(@Context SecurityContext securityContext) throws UserNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        try {
            LOGGER.info("Fetched basic info for user id: {}", authUserDto.getUserId());
            return convertUserEntitytoUserBasicInfoDto(userEntity);
        } catch (Exception e) {
            LOGGER.error("Error fetching basic info for user", e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }
    public UserBasicInfoDto getUserBasicInfo(String Username) throws UserNotFoundException {
        UserEntity userEntity = userDao.findUserByUsername(Username);
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        try {
            LOGGER.info("Fetched basic info for user id: {}", userEntity.getId());
            return convertUserEntitytoUserBasicInfoDto(userEntity);
        } catch (Exception e) {
            LOGGER.error("Error fetching basic info for user", e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of basic information for all registered users.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Fetches all user entities from the database.</li>
     *     <li>Converts the list of user entities to a list of UserBasicInfoDto objects.</li>
     * </ul>
     * </p>
     * Note: Security and access control are managed at the endpoint level.
     * </p>
     * @return a list of UserBasicInfoDto objects representing the basic information of all registered users.
     */
    public List<UserBasicInfoDto> getUsersListBasicInfo() {
        try {
            LOGGER.info("Fetching all users basic info");
            return convertUserEntityListToUserBasicInfoDtoList(userDao.findAllUsers());
        } catch (Exception e) {
            LOGGER.error("Error fetching all users basic info", e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Retrieves a list of basic information for users whose usernames start with the specified first letter.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the input to ensure it is a single alphabetic character.</li>
     *     <li>Converts the first letter to lowercase to ensure case-insensitive matching.</li>
     *     <li>Fetches the user entities from the database whose usernames start with the specified letter.</li>
     *     <li>Converts the list of user entities to a list of UserBasicInfoDto objects.</li>
     * </ul>
     * </p>
     * @param firstLetter the first letter to filter usernames by. It should be a single alphabetic character.
     * @return a list of UserBasicInfoDto objects representing the basic information of users whose usernames start with the specified letter.
     */
    public List<UserBasicInfoDto> getUsersBasicInfoByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            return new ArrayList<>();
        }
        try {
            String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
            List<UserEntity> userEntities = userDao.getUsersByFirstLetter(lowerCaseFirstLetter);
            LOGGER.info("Fetching users basic info by first letter");
            return convertUserEntityListToUserBasicInfoDtoList(userEntities);
        } catch (Exception e) {
            LOGGER.error("Error fetching users by first letter: {}", firstLetter, e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Retrieves a list of basic information for users whose usernames or first name start with the specified first letter.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the input to ensure it is a single alphabetic character.</li>
     *     <li>Converts the first letter to lowercase to ensure case-insensitive matching.</li>
     *     <li>Fetches the user entities from the database whose usernames or first name start with the specified letter.</li>
     *     <li>Converts the list of user entities to a list of UserBasicInfoDto objects.</li>
     * </ul>
     * </p>
     * @param firstLetter the first letter to filter usernames or first name by. It should be a single alphabetic character.
     * @return a list of UserMessageInfoDto objects representing the basic information of users whose usernames start with the specified letter.
     */
    public List<UserMessageInfoDto> getUserEmailRecipientByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            return new ArrayList<>();
        }
        try {
            String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
            List<UserEntity> userEntities = userDao.getUsersByFirstLetterUsernameOrFirstName(lowerCaseFirstLetter);
            LOGGER.info("Fetching users info by first letter");
            List<UserMessageInfoDto> userMessageInfoDtos = new ArrayList<>();
            for (UserEntity u : userEntities) {
                UserMessageInfoDto userMessageInfoDto = new UserMessageInfoDto();
                userMessageInfoDto.setId(u.getId());
                userMessageInfoDto.setUsername(u.getUsername());
                userMessageInfoDto.setFirstName(u.getFirstName());
                userMessageInfoDtos.add(userMessageInfoDto);
            }
            return userMessageInfoDtos;
        } catch (Exception e) {
            LOGGER.error("Error fetching users by first letter: {}", firstLetter, e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Retrieves the profile information of a user by their username.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Fetches the user entity from the database by the provided username.</li>
     *     <li>Checks if the user entity is private and if the authenticated user has permission to access it.</li>
     *     <li>Checks if the user is fetching his own profile</li>
     *     <li>Converts the user entity to a UserProfileDto object.</li>
     *     <li>Logs the operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param username the username of the user whose profile information is to be retrieved.
     * @param securityContext the security context containing the authenticated user's details.
     * @return a UserProfileDto object representing the user's profile information.
     * @throws UserNotFoundException if no user is found for the provided username.
     * @throws ForbiddenAccessException if the user profile is private and the authenticated user does not have permission to access it.
     */
    public UserProfileDto getProfileDto(String username, @Context SecurityContext securityContext) throws UserNotFoundException, ForbiddenAccessException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            throw new UserNotFoundException("No user found for this username");
        }
        if (userEntity.isPrivate() && !authUserDto.getUserId().equals(userEntity.getId())) {
            throw new ForbiddenAccessException("User is private");
        }
        try {
            LOGGER.info("Fetched profile for username: {}", username);
            return convertUserEntitytoUserProfileDto(userEntity);
        } catch (Exception e) {
            LOGGER.error("Error fetching user profile for username: {}", username, e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Updates the password of the authenticated user.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Validates the old password provided by the user.</li>
     *     <li>Encodes the new password and updates the user entity.</li>
     *     <li>Logs the operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param passwordUpdateDto the DTO containing the old and new passwords.
     * @param securityContext the security context containing the authenticated user's details.
     * @throws IllegalStateException if an error occurs while updating the password.
     * @throws UserNotFoundException if the authenticated user is not found.
     * @throws InputValidationException if the old password is incorrect or the new password is invalid.
     */
    @Transactional
    public void updatePassword(PasswordUpdateDto passwordUpdateDto, @Context SecurityContext securityContext) throws IllegalStateException, UserNotFoundException, InputValidationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!oldPasswordConfirmation(userEntity, passwordUpdateDto)) {
            throw new InputValidationException("Invalid old password or repeated new password");
        }
        try{
            String encryptedNewPassword = passEncoder.encode(passwordUpdateDto.getNewPassword());
            userEntity.setPassword(encryptedNewPassword);
            LOGGER.info("Password updated successfully");
        } catch (PersistenceException e) {
            LOGGER.error("Error while updating password at: " + e.getMessage());
            throw new IllegalStateException("Error while updating password");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Verifies that the old password provided by the user matches the stored hashed password
     * and that the new password is different from the current password.
     *
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the old and new passwords from the provided DTO.</li>
     *     <li>Compares the old password with the stored hashed password using the password encoder.</li>
     *     <li>Ensures that the new password is different from the current hashed password.</li>
     * </ul>
     * </p>
     *
     * @param userEntity the user entity containing the current hashed password.
     * @param passwordUpdateDto the DTO containing the old and new passwords.
     * @return true if the old password matches and the new password is different, false otherwise.
     */
    private boolean oldPasswordConfirmation(UserEntity userEntity, PasswordUpdateDto passwordUpdateDto) {
        String oldPassword = passwordUpdateDto.getOldPassword();
        String newPassword = passwordUpdateDto.getNewPassword();
        String hashedPassword = userEntity.getPassword();
        // Checks that the old password provided matches the hashed password and that the new password is different from the one saved
        return passEncoder.matches(oldPassword, hashedPassword) && !passEncoder.matches(newPassword, hashedPassword);
    }

    /**
     * Updates the role of a user.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the user entity from the database using the user ID provided in the DTO.</li>
     *     <li>Validates the existence of the user and the new role.</li>
     *     <li>Updates the user's role with the new role.</li>
     *     <li>Logs the operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param userUpdateRoleDto the DTO containing the user ID and the new role ID.
     * @throws InvalidCredentialsException if the user is not found.
     * @throws EntityNotFoundException if the new role is not found.
     * @throws IllegalStateException if an error occurs while updating the user role.
     */

    @Transactional
    public void updateRole(UserUpdateRoleDto userUpdateRoleDto) throws InvalidCredentialsException, UnknownHostException, EntityNotFoundException {
        UserEntity userEntity = userDao.findUserById(userUpdateRoleDto.getUserId());
        if (userEntity == null) {
            LOGGER.warn("User not found for this username");
            throw new InvalidCredentialsException("User not found with this username");
        }
        RoleEntity newRole = roleDao.findRoleById(userUpdateRoleDto.getRoleId());
        if (newRole == null) {
            throw new EntityNotFoundException("Role not found with this Id");
        }
        try{
            userEntity.setRole(newRole);
            LOGGER.info("User Role updated successfully");
        } catch (PersistenceException e) {
            LOGGER.error("Error while updating user role at: " + e.getMessage());
            throw new IllegalStateException("Error while updating user role");
        } finally {
            ThreadContext.clearMap();
        }
    }

    //TODO não estava a ser utilizado para já comentado
//    public UserBasicInfoDto getUserBasicInfoById(long userId) {
//        UserEntity userEntity = userDao.findUserById(userId);
//        return convertUserEntitytoUserBasicInfoDto(userEntity);
//    }

    /**
     * Retrieves a list of project memberships for users associated with a specific project.
     * <p>
     * This method fetches the list of project memberships from the database using the provided project ID.
     * It logs the operation for auditing purposes and handles any exceptions that may occur during the process.
     * </p>
     *
     * @param projectId the ID of the project for which to retrieve user memberships.
     * @return a list of ProjectMembershipDto objects representing the users associated with the specified project.
     * @throws RuntimeException if an error occurs while fetching the project memberships.
     */
    public List<ProjectMembershipDto> getUsersByProject(long projectId) {
        try {
            LOGGER.info("Fetching users by project");
            return projectMemberDao.getUsersByProject(projectId);
        } catch (Exception e) {
            LOGGER.error("Error fetching users by project", e);
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Creates a default user with the specified username, photo, role, and laboratory if the username does not already exist.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the username already exists in the database.</li>
     *     <li>If the username does not exist, constructs a default email and encrypted password.</li>
     *     <li>Retrieves the specified laboratory and role entities from the database.</li>
     *     <li>Creates and persists a new user entity with the provided details.</li>
     *     <li>Logs the operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param username the username of the user to be created.
     * @param photo the photo URL for the user.
     * @param roleId the ID of the role to be assigned to the user.
     * @param labId the ID of the laboratory to be assigned to the user.
     * @throws DatabaseOperationException if an error occurs while creating the user.
     * @throws IllegalStateException if the specified laboratory or role is not found.
     */

    public void createDefaultUserIfNotExistent(String username, String photo, long roleId, long labId) throws DatabaseOperationException {
        if (!userDao.checkUsernameExist(username)) {
            String email = username + "@" + username + ".com";
            String encryptedPassword = passEncoder.encode(username);
            String firstName = "a" + username;
            LaboratoryEntity laboratory = labDao.findLaboratoryById(labId);
            if (laboratory == null) {
                throw new IllegalStateException("Laboratory not found.");
            }
            RoleEntity role = roleDao.findRoleById(roleId);
            if (role == null) {
                throw new IllegalStateException("Role not found.");
            }
            try{
                LOGGER.info("Creating default user: " + username);
                UserEntity userEntity = new UserEntity(email, encryptedPassword, username, firstName, username, photo, true, false, true, laboratory, role, Instant.now());
                userDao.persist(userEntity);
            } catch (PersistenceException e) {
                throw new DatabaseOperationException("Error while creating default user" + e);
            }
        } else {
            LOGGER.info("Default user already exists: " + username);
        }
    }

    /**
     * Converts a UserRegisterDto object to a UserEntity object.
     * <p>
     * @param user the UserRegisterDto containing the user registration data.
     * @return a UserEntity object populated with the data from the DTO.
     */
    public UserEntity convertUserRegisterDtotoUserEntity(UserRegisterDto user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(user.getEmail());
        userEntity.setUsername(user.getUsername());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        return userEntity;
    }

    /**
     * Converts a UserEntity object to a UserProfileDto object.
     * <p>
     * @param userEntity the UserEntity containing the user data.
     * @return a UserProfileDto object populated with the data from the entity.
     */
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

    /**
     * Converts a UserEntity object to a UserBasicInfoDto object.
     * <p>
     * @param userEntity the UserEntity containing the user data.
     * @return a UserBasicInfoDto object populated with the data from the entity.
     */
    public UserBasicInfoDto convertUserEntitytoUserBasicInfoDto(UserEntity userEntity) {
        UserBasicInfoDto userBasicInfo = new UserBasicInfoDto();
        userBasicInfo.setId(userEntity.getId());
        userBasicInfo.setUsername(userEntity.getUsername());
        userBasicInfo.setPhoto(userEntity.getPhoto());
        userBasicInfo.setRole(userEntity.getRole().getId());
        return userBasicInfo;
    }

    /**
     * Converts a list of UserEntity objects to a list of UserBasicInfoDto objects.
     * <p>
     * @param userEntities the list of UserEntity objects to be converted.
     * @return a list of UserBasicInfoDto objects populated with the data from the UserEntity objects.
     */
    public List<UserBasicInfoDto> convertUserEntityListToUserBasicInfoDtoList(List<UserEntity> userEntities) {
        ArrayList<UserBasicInfoDto> userBasicInfoDtos = new ArrayList<>();
        for (UserEntity u : userEntities) {
            UserBasicInfoDto userBasicInfoDto = convertUserEntitytoUserBasicInfoDto(u);
            userBasicInfoDtos.add(userBasicInfoDto);
        }
        return userBasicInfoDtos;
    }
}