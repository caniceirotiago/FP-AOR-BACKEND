package aor.fpbackend.bean;

import aor.fpbackend.dao.InterestDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Interest.InterestAddDto;
import aor.fpbackend.dto.Interest.InterestGetDto;
import aor.fpbackend.dto.Interest.InterestRemoveDto;
import aor.fpbackend.entity.InterestEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.InterestTypeEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

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

    /**
     * Adds an interest to the authenticated user, ensuring the interest exists.
     *
     * @param interestAddDto   the DTO containing the interest information
     * @param securityContext  the security context to retrieve the authenticated user
     * @throws DuplicatedAttributeException if the user already has the interest or if the interest already has the user
     */
    @Transactional
    public void addInterest(InterestAddDto interestAddDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException {
        try {
            // Ensure the interest exists, creating it if necessary
            checkInterestExist(interestAddDto.getName(), interestAddDto.getType());
            // Find the interest by name
            InterestEntity interestEntity = interestDao.findInterestByName(interestAddDto.getName());
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
            } else {
                throw new DuplicatedAttributeException("User already has the specified interest");
            }

            // Add the user to the interest's users
            Set<UserEntity> interestUsers = interestEntity.getUsers();
            if (interestUsers == null) {
                interestUsers = new HashSet<>();
            }
            if (!interestUsers.contains(userEntity)) {
                interestUsers.add(userEntity);
                interestEntity.setUsers(interestUsers);
            } else {
                throw new DuplicatedAttributeException("Interest already has the specified user");
            }
            LOGGER.info("Added interest '{}' to user '{}'", interestAddDto.getName(), userEntity.getUsername());
        } catch (DuplicatedAttributeException e) {
            LOGGER.warn("Duplicate attribute: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while adding interest to user", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }

    /**
     * Ensures an interest exists by checking its existence in the database and creating it if necessary.
     *
     * @param name the name of the interest
     * @param type the type of the interest
     */
    private void checkInterestExist(String name, InterestTypeEnum type) {
        if (!interestDao.checkInterestExist(name)) {
            InterestEntity interest = new InterestEntity(name, type);
            interestDao.persist(interest);
            LOGGER.info("Created new interest: '{}'", name);
        } else {
            LOGGER.info("Interest already exists: '{}'", name);
        }
    }

    /**
     * Retrieves all interests from the database and converts them to DTOs.
     *
     * @return a list of InterestGetDto objects representing all interests
     */
    public List<InterestGetDto> getInterests() {
        try {
            List<InterestEntity> interestEntities = interestDao.getAllInterests();
            List<InterestGetDto> interestDtos = convertInterestEntityListToInterestDtoList(interestEntities);
            LOGGER.info("Successfully retrieved and converted {} interests", interestDtos.size());
            return interestDtos;
        } catch (Exception e) {
            LOGGER.error("Error retrieving interests", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }

    /**
     * Retrieves interests associated with a specific user by their username.
     *
     * @param username the username of the user whose interests are to be retrieved
     * @return a list of InterestGetDto objects representing the user's interests
     */
    public List<InterestGetDto> getInterestsByUser(String username) {
        try {
            List<InterestEntity> interestEntities = interestDao.getInterestsByUsername(username);
            List<InterestGetDto> interestDtos = convertInterestEntityListToInterestDtoList(interestEntities);
            LOGGER.info("Successfully retrieved and converted interests for user: {}", username);
            return interestDtos;
        } catch (Exception e) {
            LOGGER.error("Error retrieving interests for user: {}", username, e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }

    /**
     * Retrieves interests that start with the specified first letter.
     *
     * @param firstLetter the first letter to filter interests by
     * @return a list of InterestGetDto objects representing the interests that start with the specified letter
     */
    public List<InterestGetDto> getInterestsByFirstLetter(String firstLetter) {
        if (firstLetter == null || firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.warn("Invalid first letter input: {}", firstLetter);
            return new ArrayList<>();
        }
        try {
            String lowerCaseFirstLetter = firstLetter.toLowerCase();
            List<InterestEntity> interestEntities = interestDao.getInterestsByFirstLetter(lowerCaseFirstLetter);
            List<InterestGetDto> interestDtos = convertInterestEntityListToInterestDtoList(interestEntities);
            LOGGER.info("Successfully retrieved and converted interests starting with letter: {}", firstLetter);
            return interestDtos;
        } catch (Exception e) {
            LOGGER.error("Error retrieving interests by first letter: {}", firstLetter, e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
    }

    /**
     * Retrieves a list of all interest types defined in the InterestTypeEnum.
     *
     * @return a list of InterestTypeEnum values representing all interest types
     */
    public List<InterestTypeEnum> getEnumListInterestTypes() {
        List<InterestTypeEnum> interestTypeEnums = new ArrayList<>();
        try {
            for (InterestTypeEnum interestTypeEnum : InterestTypeEnum.values()) {
                interestTypeEnums.add(interestTypeEnum);
            }
            LOGGER.info("Successfully retrieved {} interest types", interestTypeEnums.size());
        } catch (Exception e) {
            LOGGER.error("Error retrieving interest types", e);
            throw e;
        } finally {
            ThreadContext.clearAll();
        }
        return interestTypeEnums;
    }

    /**
     * Removes an interest from a user's list of interests.
     *
     * @param interestRemoveDto  Data transfer object containing the interest to be removed.
     * @param securityContext    Security context to retrieve the authenticated user.
     * @throws UserNotFoundException   if the authenticated user is not found.
     * @throws EntityNotFoundException if the interest to be removed is not found.
     */
    @Transactional
    public void removeInterest(InterestRemoveDto interestRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        // Find the interest by name
        InterestEntity interestEntity = interestDao.findInterestById(interestRemoveDto.getId());
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
    /**
     * Converts an InterestEntity to an InterestGetDto.
     *
     * @param interestEntity The entity to be converted.
     * @return The corresponding InterestGetDto.
     */
    public InterestGetDto convertInterestEntitytoInterestDto(InterestEntity interestEntity) {
        InterestGetDto interestGetDto = new InterestGetDto();
        interestGetDto.setId(interestEntity.getId());
        interestGetDto.setName(interestEntity.getName());
        interestGetDto.setType(interestEntity.getType());
        return interestGetDto;
    }

    /**
     * Converts a list of InterestEntity objects to a list of InterestGetDto objects.
     *
     * @param interestEntities The list of InterestEntity objects to be converted.
     * @return A list of InterestGetDto objects.
     */
    public List<InterestGetDto> convertInterestEntityListToInterestDtoList(List<InterestEntity> interestEntities) {
        List<InterestGetDto> interestGetDtos = new ArrayList<>();
        for (InterestEntity i : interestEntities) {
            InterestGetDto interestGetDto = convertInterestEntitytoInterestDto(i);
            interestGetDtos.add(interestGetDto);
        }
        return interestGetDtos;
    }
}