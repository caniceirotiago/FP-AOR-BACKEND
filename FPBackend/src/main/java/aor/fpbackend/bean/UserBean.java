package aor.fpbackend.bean;

import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.LoginDto;
import aor.fpbackend.dto.TokenDto;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Stateless
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    UserDao userDao;

    @EJB
    PassEncoder passEncoder;

    @EJB
    SessionDao sessionDao;

    public boolean register(UserDto user) {
        if (user == null) return false;
        if (userDao.checkIfEmailExists(user.getEmail())) return false;
        {
            System.out.println("Invalid credentials");
        }
        try {
            UserEntity newUserEntity = convertUserDtotoUserEntity(user);
            String encryptedPassword = passEncoder.encode(user.getPassword());
            newUserEntity.setPassword(encryptedPassword);
            userDao.persist(newUserEntity);
            return true;
        } catch (NoResultException e) {
            System.out.println("Error while persisting user");
            return false;
        }
    }

    public boolean checkIfEmailExists(String email) {
        if (email != null) {
            return userDao.checkIfEmailExists(email);
        }
        return false;
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


    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

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
            userEntity.setPhoto(user.getPhotoURL());
            userEntity.setRole(user.getAppRole());
            userEntity.setDeleted(user.isDeleted());
            userEntity.setPrivate(user.isPrivate());
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
        userDto.setPhotoURL(userEntity.getPhoto());
        userDto.setAppRole(userEntity.getRole());
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
