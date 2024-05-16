package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.LoginDto;
import aor.fpbackend.dto.TokenDto;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.Column;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

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


    public boolean register(UserDto user) {
        if (user == null) return false;
        if (userDao.checkIfEmailExists(user.getEmail())) {
            System.out.println("Invalid credentials");
            return false;
        }
        try {
            UserEntity newUserEntity = convertUserDtotoUserEntity(user);
            String encryptedPassword = passEncoder.encode(user.getPassword());
            newUserEntity.setPassword(encryptedPassword);
            // Retrieve the role with ID 2 "Standard User" (as default user role)
            RoleEntity role = roleDao.findRoleById(2);
            if (role == null) {
                System.out.println("Role not found");
                return false;
            }
            // Set the role to the new user
            newUserEntity.setRole(role);
            // Retrieve the LaboratoryEntity from userDto and set it to the new user
            LaboratoryEntity lab = labDao.findLaboratoryById(user.getLaboratoryId());
            newUserEntity.setLaboratory(lab);
            // Set default parameters for the new user
            newUserEntity.setDeleted(false);
            newUserEntity.setConfirmed(false);
            newUserEntity.setPrivate(true);
            // Create a confirmation token
            String confirmationToken = generateNewToken();
            newUserEntity.setConfirmationToken(confirmationToken);
            newUserEntity.setLastSentEmailTimestamp(Instant.now());
            // Persist the new User
            userDao.persist(newUserEntity);
            // Send confirmation token by email
            emailService.sendConfirmationEmail(user.getEmail(), confirmationToken);
            return true;
        } catch (NoResultException e) {
            System.out.println("Error while persisting user");
            return false;
        }
    }

    public boolean validateUser(String token) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(token);
        if (userEntity != null) {
            if (userEntity.getConfirmationToken().equals(token)) {
                userEntity.setConfirmed(true);
                return true;
            }
        }
        return false;
    }

//    public void requestPasswordReset(String email) {
//        UserEntity user = userDao.findUserByEmail(email);
//        if (user == null) {
//        }
//        if (user.getResetPasswordTokenExpiry() != null && user.getResetPasswordTokenExpiry().isAfter(Instant.now())) {
//        }
//        String resetToken = UUID.randomUUID().toString();
//        user.setResetPasswordToken(resetToken);
//        user.setResetPasswordTokenExpiry(Instant.now().plus(30, ChronoUnit.MINUTES));
//        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
//    }

    public void resetPassword(String token, String newPassword) {
        UserEntity user = userDao.findUserByResetPasswordToken(token);
        if ((user == null) && (!user.getResetPasswordToken().equals(token))) {
            System.out.println("Invalid token");
        }
        if (user.getResetPasswordTokenExpiry().isBefore(Instant.now())) {
            System.out.println("Token expired");
        }
        String encryptedPassword = passEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
    }

    public TokenDto login(LoginDto userLogin) {
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
                TokenDto tokenDto = new TokenDto();
                tokenDto.setId(userEntity.getId());
                tokenDto.setEmail(userEntity.getEmail());
                tokenDto.setNickname(userEntity.getNickname());
                tokenDto.setToken(sessionEntity.getSessionToken());
                tokenDto.setPhoto(userEntity.getPhoto());
                System.out.println("Successfull login");
                return tokenDto;
            }
        }
        return new TokenDto();
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

//    public boolean tokenValidator(String token) {
//
//        UserEntity user = userDao.findUserByToken(token);
//        if (user != null) {
//            Instant now = Instant.now();
//            long tokenValidityPeriodInSeconds = Long.parseLong(configBean.findConfigValueByKey("sessionTimeout"));
//            Instant tokenExpiration = user.getLastActivityTimestamp().plusSeconds(tokenValidityPeriodInSeconds);
//            if (now.isBefore(tokenExpiration)) {
//                updateLastActivityTimestamp(user);
//                return true;
//            } else {
//                logout(user.getToken());
//                return false;
//            }
//        }
//        return false;
//    }


    public void logout(String token) {
        SessionEntity session = sessionDao.findSessionByToken(token);
        if (session != null) {
            sessionDao.remove(session);
        }
    }


    public ArrayList<UserDto> getAllRegUsers() {
        ArrayList<UserEntity> users = userDao.findAllUsers();
        if (users != null && !users.isEmpty()) {
            return convertUserEntityListToUserDtoList(users);
        } else {
            return new ArrayList<>();
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
