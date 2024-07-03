package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Skill.SkillAddUserDto;
import aor.fpbackend.dto.Skill.SkillGetDto;
import aor.fpbackend.dto.Skill.SkillRemoveProjectDto;
import aor.fpbackend.dto.Skill.SkillRemoveUserDto;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.enums.SkillTypeEnum;
import aor.fpbackend.exception.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SkillBean is a stateless EJB that manages the operations related to skills.
 * It interacts with various DAOs to perform CRUD operations on skill entities, user entities,
 * and project entities. This bean handles the addition, update, deletion, and retrieval
 * of skills associated with users and projects.
 *
 * <p>
 * Technologies Used:
 * <ul>
 *     <li>Jakarta EE: For dependency injection and EJB management.</li>
 *     <li>SLF4J: For logging operations.</li>
 * </ul>
 * </p>
 *
 * Dependencies are injected using the {@link EJB} annotation, which includes DAOs for user,
 * skill, and project entities. The bean also uses utility classes for logging and ensures
 * that transactions are handled appropriately.
 */
@Stateless
public class SkillBean implements Serializable {
    @EJB
    SkillDao skillDao;
    @EJB
    ProjectDao projectDao;
    @EJB
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SkillBean.class);



    /**
     * Adds a skill to the authenticated user's skill set.
     * <p>
     * This method ensures the skill exists (creating it if necessary),
     * and then associates the skill with the authenticated user. If the user
     * already possesses the skill, a DuplicatedAttributeException is thrown.
     * </p>
     * <p>
     * The method uses logging to track the process and catches any persistence
     * exceptions to handle database-related errors.
     * </p>
     *
     * @param skillAddUserDto  The DTO containing the skill name and type to add.
     * @param securityContext  The security context containing the authenticated user's information.
     * @throws DuplicatedAttributeException if the user already has the specified skill or if the skill already has the user.
     */
    @Transactional
    public void addSkillUser(SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException, DatabaseOperationException {
        LOGGER.info("Attempting to add skill '{}' of type '{}' to user.", skillAddUserDto.getName(), skillAddUserDto.getType());

        try {
            checkSkillExist(skillAddUserDto.getName(), skillAddUserDto.getType());
            SkillEntity skillEntity = skillDao.findSkillByName(skillAddUserDto.getName());
            AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
            UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());

            if (userEntity == null) {
                LOGGER.error("Authenticated user not found in database.");
                throw new EntityNotFoundException("Authenticated user not found.");
            }
            addUserSkill(userEntity, skillEntity);
            addSkillUser(skillEntity, userEntity);
            LOGGER.info("Successfully added skill '{}' to user '{}'.", skillAddUserDto.getName(), authUserDto.getUserId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while adding skill to user: {}", e.getMessage());
            throw new DatabaseOperationException("Error while adding skill to user");
        } catch (EntityNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Adds the specified skill to the user's set of skills.
     * <p>
     * This method checks if the user already has the skill and adds it if not.
     * If the user already has the skill, a DuplicatedAttributeException is thrown.
     * </p>
     *
     * @param userEntity  The user entity to which the skill will be added.
     * @param skillEntity The skill entity to be added to the user.
     * @throws DuplicatedAttributeException if the user already has the specified skill.
     */
    private void addUserSkill(UserEntity userEntity, SkillEntity skillEntity) throws DuplicatedAttributeException {
        Set<SkillEntity> userSkills = userEntity.getUserSkills();
        if (userSkills == null) {
            userSkills = new HashSet<>();
            userEntity.setUserSkills(userSkills);
        }
        if (userSkills.contains(skillEntity)) {
            LOGGER.warn("User '{}' already has the skill '{}'.", userEntity.getId(), skillEntity.getName());
            throw new DuplicatedAttributeException("User already has the specified skill");
        }
        userSkills.add(skillEntity);
    }

    /**
     * Adds the specified user to the skill's set of users.
     * <p>
     * This method checks if the skill already has the user and adds it if not.
     * If the skill already has the user, a DuplicatedAttributeException is thrown.
     * </p>
     *
     * @param skillEntity The skill entity to which the user will be added.
     * @param userEntity  The user entity to be added to the skill.
     * @throws DuplicatedAttributeException if the skill already has the specified user.
     */
    private void addSkillUser(SkillEntity skillEntity, UserEntity userEntity) throws DuplicatedAttributeException {
        Set<UserEntity> skillUsers = skillEntity.getUsers();
        if (skillUsers == null) {
            skillUsers = new HashSet<>();
            skillEntity.setUsers(skillUsers);
        }
        if (skillUsers.contains(userEntity)) {
            LOGGER.warn("Skill '{}' already has the user '{}'.", skillEntity.getName(), userEntity.getId());
            throw new DuplicatedAttributeException("Skill already has the specified user");
        }
        skillUsers.add(userEntity);
    }

    /**
     * Ensures the specified skill exists, creating it if necessary.
     * <p>
     * This method checks if a skill with the given name exists in the database.
     * If the skill does not exist, it creates a new SkillEntity and persists it.
     * </p>
     *
     * @param name  The name of the skill.
     * @param type  The type of the skill.
     */
    private void checkSkillExist(String name, SkillTypeEnum type) {
        if (!skillDao.checkSkillExist(name)) {
            SkillEntity skill = new SkillEntity(name, type);
            skillDao.persist(skill);
        }
    }

    /**
     * Adds a skill to a specified project.
     * <p>
     * This method ensures that the skill exists (creating it if necessary),
     * associates the skill with the specified project, and updates the skill's list of projects.
     * If the project already possesses the skill, a DuplicatedAttributeException is thrown.
     * </p>
     * <p>
     * The method uses logging to track the process and catches any persistence
     * exceptions to handle database-related errors.
     * </p>
     *
     * @param skillName  The name of the skill to add.
     * @param type       The type of the skill.
     * @param projectId  The ID of the project to which the skill will be added.
     * @throws DuplicatedAttributeException if the project already has the specified skill or if the skill already has the project.
     */
    @Transactional
    public void addSkillProject(String skillName, SkillTypeEnum type, long projectId) throws DuplicatedAttributeException, DatabaseOperationException, ElementAssociationException, EntityNotFoundException {
        LOGGER.info("Attempting to add skill '{}' of type '{}' to project ID '{}'.", skillName, type, projectId);
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillName, type);
        // Find the skill by name
        SkillEntity skillEntity = skillDao.findSkillByName(skillName);
        if(skillEntity == null) {
            throw new EntityNotFoundException("Skill not found.");
        }
        // Find the project by id
        ProjectEntity projectEntity = findProjectById(projectId);
        // Don't add to CANCELLED or FINISHED projects
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState == ProjectStateEnum.CANCELLED || currentState == ProjectStateEnum.FINISHED) {
            throw new ElementAssociationException("Project is not editable anymore");
        }
        try {
            // Add the skill to the project's skills
            addSkillToProject(skillEntity, projectEntity);
            // Add the project to the skill's projects
            addProjectToSkill(skillEntity, projectEntity);
            LOGGER.info("Successfully added skill '{}' to project '{}'.", skillName, projectId);
        } catch (PersistenceException e) {
            LOGGER.error("Error while adding skill to project: {}", e.getMessage());
            throw new DatabaseOperationException("Error while adding skill to project");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Finds a project by its ID.
     *
     * @param projectId the ID of the project.
     * @return the ProjectEntity if found.
     * @throws EntityNotFoundException if the project is not found.
     */
    private ProjectEntity findProjectById(long projectId) throws EntityNotFoundException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            LOGGER.error("Project with ID '{}' not found.", projectId);
            throw new EntityNotFoundException("Project not found.");
        }
        return projectEntity;
    }

    /**
     * Adds a skill to a project's skills.
     *
     * @param skillEntity the skill to add.
     * @param projectEntity the project to which the skill is added.
     * @throws DuplicatedAttributeException if the project already has the specified skill.
     */
    private void addSkillToProject(SkillEntity skillEntity, ProjectEntity projectEntity) throws DuplicatedAttributeException {
        Set<SkillEntity> projectSkills = projectEntity.getProjectSkills();
        if (projectSkills == null) {
            projectSkills = new HashSet<>();
        }
        if (!projectSkills.contains(skillEntity)) {
            projectSkills.add(skillEntity);
            projectEntity.setProjectSkills(projectSkills);
        } else {
            LOGGER.warn("Project '{}' already has the skill '{}'.", projectEntity.getId(), skillEntity.getName());
            throw new DuplicatedAttributeException("Project already has the specified skill");
        }
    }

    /**
     * Adds a project to a skill's projects.
     *
     * @param skillEntity the skill to which the project is added.
     * @param projectEntity the project to add.
     * @throws DuplicatedAttributeException if the skill already has the specified project.
     */
    private void addProjectToSkill(SkillEntity skillEntity, ProjectEntity projectEntity) throws DuplicatedAttributeException {
        Set<ProjectEntity> skillProjects = skillEntity.getProjects();
        if (skillProjects == null) {
            skillProjects = new HashSet<>();
        }
        if (!skillProjects.contains(projectEntity)) {
            skillProjects.add(projectEntity);
            skillEntity.setProjects(skillProjects);
        } else {
            LOGGER.warn("Skill '{}' already has the project '{}'.", skillEntity.getName(), projectEntity.getId());
            throw new DuplicatedAttributeException("Skill already has the specified project");
        }
    }


    /**
     * Retrieves a list of all skills in the system.
     * <p>
     * This method fetches all skill entities from the database and converts them
     * into a list of {@link SkillGetDto} objects for easier handling and presentation
     * in the application layer.
     * </p>
     * <p>
     * Logging is used to track the process, and any database-related errors are caught
     * and rethrown as a {@link DatabaseOperationException}.
     * </p>
     *
     * @return a list of {@link SkillGetDto} representing all skills in the system.
     * @throws DatabaseOperationException if there is an error while fetching the skills from the database.
     */
    public List<SkillGetDto> getSkills() throws DatabaseOperationException {
        try {
            List<SkillGetDto> skillDtos = convertSkillEntityListToSkillDtoList(skillDao.getAllSkills());
            LOGGER.info("Successfully fetched {} skills", skillDtos.size());
            return skillDtos;
        } catch (PersistenceException e) {
            LOGGER.error("Error while fetching skills: {}", e.getMessage());
            throw new DatabaseOperationException("Error while fetching skills");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of skills associated with a specific user.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Fetches the list of skills associated with the specified username from the database.</li>
     *     <li>Converts the list of skill entities to a list of {@link SkillGetDto} objects.</li>
     *     <li>Logs the number of skills fetched for the specified user.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process, and any database-related errors are caught
     * and rethrown as a {@link DatabaseOperationException}.
     * </p>
     *
     * @param username the username of the user whose skills are to be retrieved.
     * @return a list of {@link SkillGetDto} representing the skills associated with the specified user.
     * @throws EntityNotFoundException if no skills are associated with the specified user.
     * @throws DatabaseOperationException if there is an error while fetching the skills from the database.
     */
    public List<SkillGetDto> getSkillsByUser(String username) throws EntityNotFoundException, DatabaseOperationException {
        try {
            List<SkillEntity> skillEntities = skillDao.getSkillsByUsername(username);
            if (skillEntities == null || skillEntities.isEmpty()) {
                throw new EntityNotFoundException("No skills associated with this user");
            }
            List<SkillGetDto> skillDtos = convertSkillEntityListToSkillDtoList(skillEntities);
            LOGGER.info("Successfully fetched {} skills for user: {}", skillDtos.size(), username);
            return skillDtos;
        } catch (PersistenceException e) {
            LOGGER.error("Error while fetching skills for user {}: {}", username, e.getMessage());
            throw new DatabaseOperationException("Error while fetching skills for user: " + username);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of skills whose names start with a specified letter.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the input to ensure it is a single alphabetical character.</li>
     *     <li>Converts the input to lowercase to ensure case-insensitive matching.</li>
     *     <li>Fetches the list of skills from the database that start with the specified letter.</li>
     *     <li>Converts the list of skill entities to a list of {@link SkillGetDto} objects.</li>
     *     <li>Logs the number of skills fetched for the specified letter.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process, and any database-related errors are caught
     * and rethrown as a {@link DatabaseOperationException}.
     * </p>
     *
     * @param firstLetter the letter to filter skills by.
     * @return a list of {@link SkillGetDto} representing the skills starting with the specified letter, or an empty list if the input is invalid.
     * @throws DatabaseOperationException if there is an error while fetching the skills from the database.
     */
    public List<SkillGetDto> getSkillsByFirstLetter(String firstLetter) throws DatabaseOperationException {
        try {
            if (firstLetter == null || firstLetter.length() != 1) {
                return new ArrayList<>();
            }
            String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
            List<SkillEntity> skillEntities = skillDao.getSkillsByFirstLetter(lowerCaseFirstLetter);
            List<SkillGetDto> skillDtos = convertSkillEntityListToSkillDtoList(skillEntities);
            LOGGER.info("Successfully fetched {} skills starting with the letter: {}", skillDtos.size(), firstLetter);
            return skillDtos;
        } catch (PersistenceException e) {
            throw new DatabaseOperationException("Error while fetching skills starting with the letter: " + firstLetter);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of skills associated with a specific project.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Fetches the list of skills associated with the specified project ID from the database.</li>
     *     <li>Converts the list of skill entities to a list of {@link SkillGetDto} objects.</li>
     *     <li>Logs the number of skills fetched for the specified project ID.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process, and any database-related errors are caught
     * and rethrown as a {@link DatabaseOperationException}.
     * </p>
     *
     * @param projectId the ID of the project whose skills are to be retrieved.
     * @return a list of {@link SkillGetDto} representing the skills associated with the specified project.
     * @throws DatabaseOperationException if there is an error while fetching the skills from the database.
     */
    public List<SkillGetDto> getSkillsByProject(long projectId) throws DatabaseOperationException {
        try {
            List<SkillEntity> skillEntities = skillDao.getSkillsByProjectId(projectId);
            List<SkillGetDto> skillDtos = convertSkillEntityListToSkillDtoList(skillEntities);
            LOGGER.info("Successfully fetched {} skills for project ID: {}", skillDtos.size(), projectId);
            return skillDtos;
        } catch (PersistenceException e) {
            LOGGER.error("Error while fetching skills for project ID {}: {}", projectId, e.getMessage());
            throw new DatabaseOperationException("Error while fetching skills for project ID: " + projectId);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of all skill type enumerations.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Creates a new list to hold the {@link SkillTypeEnum} values.</li>
     *     <li>Iterates over all values of the {@link SkillTypeEnum} enumeration and adds them to the list.</li>
     *     <li>Logs the retrieval of the skill type enumerations.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process.
     * </p>
     *
     * @return a list of {@link SkillTypeEnum} representing all possible skill types.
     */
    public List<SkillTypeEnum> getEnumListSkillTypes() {
        try {
            List<SkillTypeEnum> skillTypeEnums = new ArrayList<>(Arrays.asList(SkillTypeEnum.values()));
            LOGGER.info("Successfully fetched {} skill type enumerations", skillTypeEnums.size());
            return skillTypeEnums;
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Removes a skill from a user's list of skills.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Validates the existence of the user and skill.</li>
     *     <li>Removes the skill from the user's list of skills.</li>
     *     <li>Removes the user from the skill's list of users.</li>
     *     <li>Logs the successful removal of the skill from the user.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process.
     * </p>
     *
     * @param skillRemoveUserDto the DTO containing the skill ID to be removed from the user.
     * @param securityContext the security context containing the authenticated user's details.
     * @throws UserNotFoundException if the authenticated user is not found.
     * @throws EntityNotFoundException if the skill is not found.
     */
    @Transactional
    public void removeSkillUser(SkillRemoveUserDto skillRemoveUserDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        LOGGER.info("Attempting to remove skill ID: {} from user", skillRemoveUserDto.getId());
        try {
            AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
            UserEntity userEntity = getUserEntity(authUserDto);
            SkillEntity skillEntity = getSkillEntity(skillRemoveUserDto);
            removeSkillFromUser(userEntity, skillEntity);
            removeUserFromSkill(skillEntity, userEntity);
            LOGGER.info("Successfully removed skill ID: {} from user", skillRemoveUserDto.getId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while removing skill ID: {} from user: {}", skillRemoveUserDto.getId(), e.getMessage());
            throw new EntityNotFoundException("Error while removing skill from user");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves the authenticated user entity from the database.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details using their ID.</li>
     *     <li>Validates the existence of the user.</li>
     *     <li>Logs the successful retrieval of the user.</li>
     * </ul>
     * </p>
     *
     * @param authUserDto the authenticated user's DTO.
     * @return the UserEntity representing the authenticated user.
     * @throws UserNotFoundException if the authenticated user is not found.
     */
    private UserEntity getUserEntity(AuthUserDto authUserDto) throws UserNotFoundException {
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        LOGGER.debug("Authenticated user found: {}", userEntity.getUsername());
        return userEntity;
    }


    /**
     * Retrieves the skill entity from the database.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the skill entity using the provided skill ID.</li>
     *     <li>Validates the existence of the skill.</li>
     *     <li>Logs the successful retrieval of the skill.</li>
     * </ul>
     * </p>
     *
     * @param skillRemoveUserDto the DTO containing the skill ID to be retrieved.
     * @return the SkillEntity representing the skill to be removed.
     * @throws EntityNotFoundException if the skill is not found.
     */
    private SkillEntity getSkillEntity(SkillRemoveUserDto skillRemoveUserDto) throws EntityNotFoundException {
        SkillEntity skillEntity = skillDao.findSkillById(skillRemoveUserDto.getId());
        if (skillEntity == null) {
            throw new EntityNotFoundException("Skill not found");
        }
        LOGGER.debug("Skill found: {}", skillEntity.getName());
        return skillEntity;
    }


    /**
     * Removes a skill from the user's list of skills.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the user's current list of skills.</li>
     *     <li>Checks if the skill is present in the user's list of skills.</li>
     *     <li>Removes the skill from the user's list of skills and updates the user entity.</li>
     *     <li>Logs the successful removal of the skill from the user's list of skills.</li>
     * </ul>
     * </p>
     *
     * @param userEntity the user entity from which the skill will be removed.
     * @param skillEntity the skill entity to be removed from the user's list of skills.
     */
    private void removeSkillFromUser(UserEntity userEntity, SkillEntity skillEntity) {
        Set<SkillEntity> userSkills = userEntity.getUserSkills();
        if (userSkills.contains(skillEntity)) {
            userSkills.remove(skillEntity);
            userEntity.setUserSkills(userSkills);
            LOGGER.debug("Skill removed from user's skills");
        } else {
            throw new IllegalStateException("User does not have the specified skill");
        }
    }


    /**
     * Removes a user from the skill's list of users.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the skill's current list of users.</li>
     *     <li>Removes the user from the skill's list of users and updates the skill entity.</li>
     *     <li>Logs the successful removal of the user from the skill's list of users.</li>
     * </ul>
     * </p>
     *
     * @param skillEntity the skill entity from which the user will be removed.
     * @param userEntity the user entity to be removed from the skill's list of users.
     */
    private void removeUserFromSkill(SkillEntity skillEntity, UserEntity userEntity) {
        Set<UserEntity> skillUsers = skillEntity.getUsers();
        skillUsers.remove(userEntity);
        skillEntity.setUsers(skillUsers);
        LOGGER.debug("User removed from skill's users");
    }


    /**
     * Removes a skill from a project's list of skills.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the project entity using the project ID from the DTO.</li>
     *     <li>Retrieves the skill entity using the skill ID from the DTO.</li>
     *     <li>Removes the skill from the project's list of skills.</li>
     *     <li>Removes the project from the skill's list of projects.</li>
     *     <li>Logs the successful removal of the skill from the project.</li>
     * </ul>
     * </p>
     * <p>
     * Logging is used to track the process, and thread context is cleared after execution.
     * </p>
     *
     * @param skillRemoveProjectDto the DTO containing the skill ID and project ID.
     * @throws EntityNotFoundException if the project or skill is not found.
     */
    @Transactional
    public void removeSkillProject(SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        try {
            ProjectEntity projectEntity = getProjectEntity(skillRemoveProjectDto);
            SkillEntity skillEntity = getSkillEntity(skillRemoveProjectDto);
            removeSkillFromProject(projectEntity, skillEntity);
            removeProjectFromSkill(skillEntity, projectEntity);
            LOGGER.info("Successfully removed skill ID: {} from project ID: {}", skillRemoveProjectDto.getId(), skillRemoveProjectDto.getProjectId());
        } catch (PersistenceException e) {
            LOGGER.error("Error while removing skill ID: {} from project ID: {}: {}", skillRemoveProjectDto.getId(), skillRemoveProjectDto.getProjectId(), e.getMessage());
            throw new EntityNotFoundException("Error while removing skill from project");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves the project entity from the database.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the project entity using the provided project ID.</li>
     *     <li>Validates the existence of the project.</li>
     *     <li>Logs the successful retrieval of the project.</li>
     * </ul>
     * </p>
     *
     * @param skillRemoveProjectDto the DTO containing the project ID.
     * @return the ProjectEntity representing the project to be updated.
     * @throws EntityNotFoundException if the project is not found.
     */
    private ProjectEntity getProjectEntity(SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        ProjectEntity projectEntity = projectDao.findProjectById(skillRemoveProjectDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        LOGGER.debug("Project found: {}", projectEntity.getName());
        return projectEntity;
    }


    /**
     * Retrieves the skill entity from the database.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the skill entity using the provided skill ID.</li>
     *     <li>Validates the existence of the skill.</li>
     *     <li>Logs the successful retrieval of the skill.</li>
     * </ul>
     * </p>
     *
     * @param skillRemoveProjectDto the DTO containing the skill ID.
     * @return the SkillEntity representing the skill to be updated.
     * @throws EntityNotFoundException if the skill is not found.
     */
    private SkillEntity getSkillEntity(SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        SkillEntity skillEntity = skillDao.findSkillById(skillRemoveProjectDto.getId());
        if (skillEntity == null) {
            throw new EntityNotFoundException("Skill not found");
        }
        LOGGER.debug("Skill found: {}", skillEntity.getName());
        return skillEntity;
    }


    /**
     * Removes a skill from the project's list of skills.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the project's current list of skills.</li>
     *     <li>Checks if the skill is present in the project's list of skills.</li>
     *     <li>Removes the skill from the project's list of skills and updates the project entity.</li>
     *     <li>Logs the successful removal of the skill from the project's list of skills.</li>
     * </ul>
     * </p>
     *
     * @param projectEntity the project entity from which the skill will be removed.
     * @param skillEntity the skill entity to be removed from the project's list of skills.
     */
    private void removeSkillFromProject(ProjectEntity projectEntity, SkillEntity skillEntity) {
        Set<SkillEntity> projectSkills = projectEntity.getProjectSkills();
        if (projectSkills.contains(skillEntity)) {
            projectSkills.remove(skillEntity);
            projectEntity.setProjectSkills(projectSkills);
            LOGGER.debug("Skill removed from project's skills");
        } else {
            throw new IllegalStateException("Project does not have the specified skill");
        }
    }

    /**
     * Removes a project from the skill's list of projects.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the skill's current list of projects.</li>
     *     <li>Checks if the project is present in the skill's list of projects.</li>
     *     <li>Removes the project from the skill's list of projects and updates the skill entity.</li>
     *     <li>Logs the successful removal of the project from the skill's list of projects.</li>
     * </ul>
     * </p>
     *
     * @param skillEntity the skill entity from which the project will be removed.
     * @param projectEntity the project entity to be removed from the skill's list of projects.
     */
    private void removeProjectFromSkill(SkillEntity skillEntity, ProjectEntity projectEntity) {
        Set<ProjectEntity> skillProjects = skillEntity.getProjects();
        if (skillProjects.contains(projectEntity)) {
            skillProjects.remove(projectEntity);
            skillEntity.setProjects(skillProjects);
            LOGGER.debug("Project removed from skill's projects");
        } else {
            throw new IllegalStateException("Skill does not have the specified project");
        }
    }



    /**
     * Converts a SkillEntity object to a SkillGetDto object.
     *
     * @param skillEntity the SkillEntity object to be converted.
     * @return a SkillGetDto object populated with the data from the SkillEntity.
     * @throws NullPointerException if the skillEntity is null.
     */
    public SkillGetDto convertSkillEntitytoSkillDto(SkillEntity skillEntity) {
        // Ensure the input skillEntity is not null
        if (skillEntity == null) {
            throw new NullPointerException("The skillEntity cannot be null.");
        }

        // Initialize the result DTO
        SkillGetDto skillGetDto = new SkillGetDto();

        // Populate the DTO fields
        skillGetDto.setId(skillEntity.getId());
        skillGetDto.setName(skillEntity.getName());
        skillGetDto.setType(skillEntity.getType());

        return skillGetDto;
    }

    /**
     * Converts a list of SkillEntity objects to a list of SkillGetDto objects.
     *
     * @param skillEntities the list of SkillEntity objects to be converted.
     * @return a list of SkillGetDto objects populated with the data from the SkillEntity objects.
     * @throws NullPointerException if the input list is null.
     */
    public List<SkillGetDto> convertSkillEntityListToSkillDtoList(List<SkillEntity> skillEntities) {
        // Ensure the input list is not null
        if (skillEntities == null) {
            throw new NullPointerException("The skillEntities list cannot be null.");
        }

        // Convert the list using Stream API
        return skillEntities.stream()
                .map(this::convertSkillEntitytoSkillDto)
                .collect(Collectors.toList());
    }
}
