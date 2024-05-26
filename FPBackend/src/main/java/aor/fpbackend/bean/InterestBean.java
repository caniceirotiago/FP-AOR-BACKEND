package aor.fpbackend.bean;

import aor.fpbackend.dao.InterestDao;
import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.dto.SkillDto;
import aor.fpbackend.entity.InterestEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class InterestBean implements Serializable {
    @EJB
    InterestDao interestDao;
    @EJB
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(InterestBean.class);

    @Transactional
    public void addInterest(InterestDto interestDto, @Context SecurityContext securityContext) {
        // Ensure the interest exists, creating it if necessary
        checkInterestExist(interestDto.getName());
        // Find the interest by name
        InterestEntity interestEntity = interestDao.findInterestByName(interestDto.getName());
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        // Add the interest to the user's interests
        Set<InterestEntity> userInterests = userEntity.getUserInterests();
        if (userInterests == null) {
            userInterests = new HashSet<>();
        }
        if (!userInterests.contains(interestEntity)) {
            userInterests.add(interestEntity);
            userEntity.setUserInterests(userInterests);
        }
        // Add the user to the interest's users
        Set<UserEntity> interestUsers = interestEntity.getUsers();
        if (interestUsers == null) {
            interestUsers = new HashSet<>();
        }
        if (!interestUsers.contains(userEntity)) {
            interestUsers.add(userEntity);
            interestEntity.setUsers(interestUsers);
        }
    }

    private void checkInterestExist(String name) {
        if (!interestDao.checkInterestExist(name)) {
            InterestEntity interest = new InterestEntity(name);
            interestDao.persist(interest);
        }
    }

    public List<InterestDto> getInterests() {
        return convertInterestEntityListToInterestDtoList(interestDao.getAllInterests());
    }

    public List<InterestDto> getInterestsByUser(@Context SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        return convertInterestEntityListToInterestDtoList(interestDao.getInterestsByUserId(authUserDto.getUserId()));
    }

    public List<InterestDto> getInterestsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        return convertInterestEntityListToInterestDtoList(interestDao.getInterestsByFirstLetter(firstLetter.charAt(0)));
    }

    @Transactional
    public void removeInterest(InterestDto interestDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        // Find the interest by name
        InterestEntity interestEntity = interestDao.findInterestById(interestDto.getId());
        if (interestEntity == null) {
            throw new EntityNotFoundException("Interest not found");
        }
        // Remove the interest from the user's interests
        Set<InterestEntity> userInterests = userEntity.getUserInterests();
        if (userInterests.contains(interestEntity)) {
            userInterests.remove(interestEntity);
            userEntity.setUserInterests(userInterests);

            // Remove the user from the interest's users
            Set<UserEntity> interestUsers = interestEntity.getUsers();
            interestUsers.remove(userEntity);
            interestEntity.setUsers(interestUsers);
        } else {
            throw new IllegalStateException("User does not have the specified interest");
        }
    }

    public InterestDto convertInterestEntitytoInterestDto(InterestEntity interestEntity) {
        InterestDto interestDto = new InterestDto();
        interestDto.setId(interestEntity.getId());
        interestDto.setName(interestEntity.getName());
        return interestDto;
    }

    public List<InterestDto> convertInterestEntityListToInterestDtoList(List<InterestEntity> interestEntities) {
        List<InterestDto> interestDtos = new ArrayList<>();
        for (InterestEntity i : interestEntities) {
            InterestDto interestDto = convertInterestEntitytoInterestDto(i);
            interestDtos.add(interestDto);
        }
        return interestDtos;
    }
}