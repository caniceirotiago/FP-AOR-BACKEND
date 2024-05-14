package aor.fpbackend.bean;

import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    UserDao userDao;

    @EJB
    PassEncoder passEncoder;

    public boolean register(UserDto user) {
        if(user == null) return false;
        if (userDao.checkIfEmailExists(user.getEmail()))return false; {
            System.out.println("Invalid credentials");
        }
        try {
        UserEntity newUserEntity = convertUserDtotoUserEntity(user);
        String encryptedPassword = passEncoder.encode(user.getPassword());
        newUserEntity.setPassword(encryptedPassword);
        userDao.persist(newUserEntity);
            return true;
        } catch (NoResultException e ) {
            System.out.println("Error while persisting user");
            return false;
        }
    }

    public boolean checkIfEmailExists(String email){
        if(email !=null){
            return userDao.checkIfEmailExists(email);
        }
        return false;
    }


    public TokenDto login(LoginDto user) throws InvalidLoginException, UnknownHostException {
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if(userEntity !=null){
            if (userEntity.getDeleted()) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to login with deleted user at: " + user.getUsername());
                throw new InvalidLoginException("User is deleted - contact the administrator");
            }
            if(!userEntity.isConfirmed()){
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with unconfirmed user at: " + user.getUsername());
                throw new InvalidLoginException("User is not confirmed - check your email for confirmation link or resend confirmation email");
            }
            String hashedPassword = HashUtil.toSHA256(user.getPassword());
            if (!userEntity.getPassword().equals(hashedPassword)){
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with invalid password at: " + user.getUsername());
                throw new InvalidLoginException("Invalid Credentials");
            }
            String token = generateNewToken();
            userEntity.setToken(token);
            updateLastActivityTimestamp(userEntity);
            return new TokenDto(token);
        }
        LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with invalid username at: " + user.getUsername());
        throw new InvalidLoginException("Invalid Credentials");
    }
    public boolean tokenValidator(String token) throws UserNotFoundException, UnknownHostException {

        UserEntity user = userDao.findUserByToken(token);
        if (user != null) {
            Instant now = Instant.now();
            long tokenValidityPeriodInSeconds = Long.parseLong(configBean.findConfigValueByKey("sessionTimeout"));
            Instant tokenExpiration = user.getLastActivityTimestamp().plusSeconds(tokenValidityPeriodInSeconds);
            if (now.isBefore(tokenExpiration)) {
                updateLastActivityTimestamp(user);
                return true;
            } else {
                logout(user.getToken());
                return false;
            }
        }
        return false;
    }

    private void updateLastActivityTimestamp(UserEntity user) {
        user.setLastActivityTimestamp(Instant.now());
        userDao.updateUser(user);
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public void logout(String token) throws UserNotFoundException, UnknownHostException {
        UserEntity user = getUserByToken(token);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to logout with invalid token at " + LocalDateTime.now() + ": " + token);
            throw new UserNotFoundException("User not found");
        }
        user.setToken(null);
        statistiscsBean.broadcastUserStatisticsUpdate();
        userDao.updateUser(user);
    }

    public InitialInformationDto getUserBasicInfo(String token) throws UserNotFoundException, UnknownHostException {
        UserEntity user = userDao.findUserByToken(token);
        if(user == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to get user basic info with invalid token at: " + token);
            throw new UserNotFoundException("Invalid Credentials");

        }
        return new InitialInformationDto(user.getPhotoURL(), user.getFirstName(), user.getRole(), user.getUsername());
    }


    private UserEntity convertUserDtotoUserEntity(UserDto user){
        UserEntity userEntity = new UserEntity();
        if(userEntity != null){
            userEntity.setUsername(user.getUsername());
            userEntity.setPassword(user.getPassword());
            userEntity.setEmail(user.getEmail());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setPhoneNumber(user.getPhoneNumber());
            userEntity.setToken(null);
            userEntity.setPhotoURL(user.getPhotoURL());
            userEntity.setRole(user.getRole());
            userEntity.setDeleted(false);
            userEntity.setConfirmed(false);
            userEntity.setConfirmationToken(user.getConfirmationToken());

            return userEntity;
        }
        return null;
    }


}
