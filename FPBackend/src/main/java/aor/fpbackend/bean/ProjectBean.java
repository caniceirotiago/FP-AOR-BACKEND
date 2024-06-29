package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Keyword.KeywordAddDto;
import aor.fpbackend.dto.Project.*;
import aor.fpbackend.dto.Skill.SkillAddProjectDto;
import aor.fpbackend.dto.User.UsernameDto;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.*;
import aor.fpbackend.exception.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectBean is a stateless session bean responsible for managing project-related operations within the system.
 * <p>
 * This bean handles the creation, updating, retrieval, and logging of projects. It interacts with various DAOs to perform
 * CRUD operations on project entities, user entities, and related entities.
 * </p>
 * <p>
 * Key functionalities provided by this bean include:
 * <ul>
 *     <li>Creating new projects and defining their relationships with users, skills, keywords, and assets.</li>
 *     <li>Updating existing projects with new details and managing their state transitions.</li>
 *     <li>Retrieving project details and converting them to DTOs for presentation purposes.</li>
 *     <li>Logging project-related activities and changes for auditing and tracking purposes.</li>
 * </ul>
 * </p>
 * <p>
 * The class uses dependency injection to obtain instances of various beans and DAOs, promoting a clean architecture
 * and separation of concerns.
 * </p>
 * <p>
 * Technologies Used:
 * <ul>
 *     <li><b>Jakarta EE</b>: For EJB and transaction management.</li>
 *     <li><b>Log4j</b>: For logging operations.</li>
 * </ul>
 * </p>
 * <p>
 *
 * @see ProjectDao
 * @see UserDao
 * @see SkillBean
 * @see KeywordBean
 * @see AssetBean
 * @see TaskBean
 * @see NotificationBean
 */

@Stateless
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ProjectBean.class);

    @EJB
    ProjectDao projectDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    UserDao userDao;
    @EJB
    ProjectLogDao projectLogDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    UserBean userBean;
    @EJB
    SkillBean skillBean;
    @EJB
    KeywordBean keywordBean;
    @EJB
    AssetBean assetBean;
    @EJB
    LaboratoryBean laboratoryBean;
    @EJB
    TaskBean taskBean;
    @EJB
    MembershipBean memberBean;
    @EJB
    NotificationBean notificationBean;


    /**
     * Creates a new project with the provided details.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the conclusion date and keywords.</li>
     *     <li>Retrieves the authenticated user and validates existence.</li>
     *     <li>Retrieves the laboratory entity and validates existence.</li>
     *     <li>Checks for duplicate project name.</li>
     *     <li>Creates a new project entity and persists it.</li>
     *     <li>Defines relations for users, skills, keywords, and assets.</li>
     *     <li>Logs the creation of the project.</li>
     * </ul>
     * </p>
     *
     * @param projectCreateDto the DTO containing project creation details.
     * @param securityContext  the security context containing the authenticated user's details.
     * @throws EntityNotFoundException if the user or laboratory is not found.
     * @throws DuplicatedAttributeException if the project name is duplicated.
     * @throws InputValidationException if the input validation fails.
     * @throws UserNotFoundException if the authenticated user is not found.
     * @throws ElementAssociationException if there is an error in associating elements.
     * @throws UnknownHostException if there is an unknown host exception.
     * @throws DatabaseOperationException if there is an error persisting the project.
     */
    @Transactional
    public void createProject(ProjectCreateDto projectCreateDto, SecurityContext securityContext)
            throws EntityNotFoundException, DuplicatedAttributeException, InputValidationException,
            UserNotFoundException, ElementAssociationException, UnknownHostException,
            DatabaseOperationException {
        LOGGER.info("Creating project: {}", projectCreateDto.getName());
        try {
            validateProjectCreateDto(projectCreateDto);

            AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
            UserEntity user = getUserById(authUserDto.getUserId());

            LaboratoryEntity laboratoryEntity = getLaboratoryById(projectCreateDto.getLaboratoryId());

            if (projectDao.checkProjectNameExist(projectCreateDto.getName())) {
                throw new InputValidationException("Duplicated project name");
            }

            ProjectEntity projectEntity = new ProjectEntity();
            populateProjectEntity(projectCreateDto, projectEntity, user, laboratoryEntity);
            projectDao.persist(projectEntity);

            ProjectEntity persistedProject = projectDao.findProjectByName(projectEntity.getName());
            addRelationsToProject(projectCreateDto, persistedProject, user);

            String content = "Creation of " + projectEntity.getName();
            createProjectLog(projectEntity, user, LogTypeEnum.GENERAL_PROJECT_DATA, content);

            LOGGER.info("Project created successfully: {}", projectCreateDto.getName());
        } catch (PersistenceException e) {
            LOGGER.error("Error while creating project: {}", e.getMessage());
            throw new DatabaseOperationException("Error while creating project");
        } finally {
            ThreadContext.clearMap();
        }
    }

    private void validateProjectCreateDto(ProjectCreateDto projectCreateDto) throws InputValidationException {
        if (projectCreateDto.getConclusionDate() != null && projectCreateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new InputValidationException("Conclusion date cannot be in the past");
        }
        if (projectCreateDto.getKeywords() == null || projectCreateDto.getKeywords().isEmpty()) {
            throw new InputValidationException("Define at least one keyword");
        }
    }

    private UserEntity getUserById(Long userId) throws UserNotFoundException {
        UserEntity user = userDao.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User with Id: " + userId + " not found");
        }
        return user;
    }

    private LaboratoryEntity getLaboratoryById(Long laboratoryId) throws EntityNotFoundException {
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(laboratoryId);
        if (laboratoryEntity == null) {
            throw new EntityNotFoundException("Lab with Id: " + laboratoryId + " not found");
        }
        return laboratoryEntity;
    }

    private void populateProjectEntity(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity,
                                       UserEntity user, LaboratoryEntity laboratoryEntity) {
        projectEntity.setName(projectCreateDto.getName());
        projectEntity.setDescription(projectCreateDto.getDescription());
        projectEntity.setMotivation(projectCreateDto.getMotivation());
        projectEntity.setConclusionDate(projectCreateDto.getConclusionDate());
        projectEntity.setLaboratory(laboratoryEntity);
        projectEntity.setCreatedBy(user);
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setCreationDate(Instant.now());
        projectEntity.setApproved(false);
    }


    /**
     * Adds various relations (users, skills, keywords, assets) to the newly created project.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Adds the project creator to the project members.</li>
     *     <li>Defines relations for project members (users) if provided.</li>
     *     <li>Defines relations for project skills if provided.</li>
     *     <li>Defines relations for project keywords if provided.</li>
     *     <li>Defines relations for project assets if provided.</li>
     *     <li>Adds a default final task to the project.</li>
     * </ul>
     * </p>
     *
     * @param projectCreateDto the DTO containing project creation details.
     * @param projectEntity    the project entity to which relations are to be added.
     * @param userCreator      the user entity representing the creator of the project.
     * @throws EntityNotFoundException       if an entity related to the project is not found.
     * @throws DuplicatedAttributeException  if there are duplicate attributes in the project relations.
     * @throws UserNotFoundException         if a user related to the project is not found.
     * @throws InputValidationException      if there is an input validation error.
     * @throws ElementAssociationException   if there is an error in associating elements.
     * @throws UnknownHostException          if there is an unknown host exception.
     * @throws DatabaseOperationException    if there is an error in the database operation.
     */
    @Transactional
    private void addRelationsToProject(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity, UserEntity userCreator) throws EntityNotFoundException, DuplicatedAttributeException, UserNotFoundException, InputValidationException, ElementAssociationException, UnknownHostException, DatabaseOperationException {
        // Add creator to project
        memberBean.addUserToProject(userCreator.getUsername(), projectEntity.getId(), true, true, userCreator);
        // Define relations for project members (Users)
        if (projectCreateDto.getUsers() != null && !projectCreateDto.getUsers().isEmpty()) {
            Set<String> usernames = projectCreateDto.getUsers().stream().map(UsernameDto::getUsername).collect(Collectors.toSet());
            for (String username : usernames) {
                memberBean.addUserToProject(username, projectEntity.getId(), true, false, userCreator);

            }
        }
        // Define relations for project Skills
        if (projectCreateDto.getSkills() != null && !projectCreateDto.getSkills().isEmpty()) {
            Set<SkillAddProjectDto> skills = projectCreateDto.getSkills().stream().collect(Collectors.toSet());
            for (SkillAddProjectDto skill : skills) {
                String skillName = skill.getName();
                SkillTypeEnum skillType = skill.getType();
                skillBean.addSkillProject(skillName, skillType, projectEntity.getId());
            }
        }
        // Define relations for project Keywords
        if (projectCreateDto.getKeywords() != null && !projectCreateDto.getKeywords().isEmpty()) {
            Set<KeywordAddDto> keywords = projectCreateDto.getKeywords().stream().collect(Collectors.toSet());
            for (KeywordAddDto keyword : keywords) {
                String keywordName = keyword.getName();
                keywordBean.addKeyword(keywordName, projectEntity.getId());
            }
        }
        // Define relations for project ProjectAssets
        if (projectCreateDto.getAssets() != null && !projectCreateDto.getAssets().isEmpty()) {
            Set<ProjectAssetCreateDto> assets = projectCreateDto.getAssets().stream().collect(Collectors.toSet());
            for (ProjectAssetCreateDto asset : assets) {
                String assetName = asset.getName();
                int assetUsedQuantity = asset.getUsedQuantity();
                assetBean.addProjectAssetToProject(assetName, projectEntity.getId(), assetUsedQuantity);
            }
        }
        // Define default final Task
        String title = "Final Presentation nº" + projectEntity.getId();
        String description = "Presentation of project: " + projectEntity.getName();
        Instant plannedEndDate = projectEntity.getConclusionDate();
        Instant plannedStartDate = plannedEndDate.minus(1, ChronoUnit.DAYS);
        taskBean.addTask(title, description, plannedStartDate, plannedEndDate, userCreator.getId(), projectEntity.getId());
    }

    /**
     * Retrieves a list of all projects in the system.
     * <p>
     * This method fetches all project entities from the database and converts them into a list of
     * {@link ProjectGetDto} objects for easier handling and presentation in the application layer.
     * </p>
     *
     * @return an {@link ArrayList} of {@link ProjectGetDto} representing all projects in the system.
     */
    public ArrayList<ProjectGetDto> getAllProjects() {
        try {
            ArrayList<ProjectEntity> projects = projectDao.findAllProjects();
            if (projects != null && !projects.isEmpty()) {
                ArrayList<ProjectGetDto> projectDtos = convertProjectEntityListToProjectDtoList(projects);
                LOGGER.info("Successfully fetched {} projects", projectDtos.size());
                return projectDtos;
            } else {
                LOGGER.warn("No projects found in the database");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching projects: {}", e.getMessage());
            return new ArrayList<>();
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves the details of a specific project by its ID.
     * <p>
     * This method fetches the project entity associated with the provided project ID from the database
     * and converts it into a {@link ProjectGetDto} object for easier handling and presentation in the application layer.
     * If the project is not found, an {@link EntityNotFoundException} is thrown.
     * </p>
     *
     * @param projectId the ID of the project to be retrieved.
     * @return a {@link ProjectGetDto} object representing the details of the specified project.
     * @throws EntityNotFoundException if the project is not found.
     */
    public ProjectGetDto getProjectDetailsById(long projectId) throws EntityNotFoundException {
        try {
            ProjectEntity projectEntity = projectDao.findProjectById(projectId);
            if (projectEntity != null) {
                ProjectGetDto projectGetDto = convertProjectEntityToProjectDto(projectEntity);
                LOGGER.info("Successfully fetched details for project ID: {}", projectId);
                return projectGetDto;
            } else {
                LOGGER.warn("Project with ID: {} not found", projectId);
                throw new EntityNotFoundException("Project not found");
            }
        } catch (EntityNotFoundException e) {
            LOGGER.error("Entity not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error fetching project details: {}", e.getMessage());
            throw new RuntimeException("Error fetching project details", e);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of all possible project states.
     * <p>
     * This method iterates over all values of the {@link ProjectStateEnum} enumeration and adds them
     * to a list, which is then returned. This is useful for populating dropdowns or other UI elements
     * that need to display the possible states a project can be in.
     * </p>
     *
     * @return a list of {@link ProjectStateEnum} values representing all possible project states.
     */
    public List<ProjectStateEnum> getEnumListProjectStates() {
        List<ProjectStateEnum> projectStateEnums = new ArrayList<>();
        try {
            for (ProjectStateEnum projectStateEnum : ProjectStateEnum.values()) {
                projectStateEnums.add(projectStateEnum);
            }
            LOGGER.info("Successfully fetched {} project states.", projectStateEnums.size());
            return projectStateEnums;
        } catch (Exception e) {
            LOGGER.error("Error fetching project states: {}", e.getMessage());
            throw new RuntimeException("Error fetching project states", e);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of all possible project roles.
     * <p>
     * This method iterates over all values of the {@link ProjectRoleEnum} enumeration and adds them
     * to a list, which is then returned. This is useful for populating dropdowns or other UI elements
     * that need to display the possible roles within a project.
     * </p>
     *
     * @return a list of {@link ProjectRoleEnum} values representing all possible project roles.
     */
    public List<ProjectRoleEnum> getEnumListProjectRoles() {
        List<ProjectRoleEnum> projectRoleEnums = new ArrayList<>();
        try {
            for (ProjectRoleEnum projectRoleEnum : ProjectRoleEnum.values()) {
                projectRoleEnums.add(projectRoleEnum);
            }
            LOGGER.info("Successfully fetched {} project roles.", projectRoleEnums.size());
            return projectRoleEnums;
        } catch (Exception e) {
            LOGGER.error("Error fetching project roles: {}", e.getMessage());
            throw new RuntimeException("Error fetching project roles", e);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a list of project logs for a specific project.
     * <p>
     * This method fetches all log entries associated with the given project ID from the database,
     * converts each log entry to a {@link ProjectLogGetDto} object, and returns the list of DTOs.
     * </p>
     * <p>
     * Logging is performed to track the fetching process and any potential errors.
     * </p>
     *
     * @param projectId the ID of the project whose logs are to be retrieved.
     * @return a list of {@link ProjectLogGetDto} representing the logs of the specified project.
     */
    public List<ProjectLogGetDto> getListProjectLogs(long projectId) {
        List<ProjectLogEntity> projectLogEntities = projectLogDao.findProjectLogsByProjectId(projectId);
        List<ProjectLogGetDto> projectLogGetDtos = new ArrayList<>();
        try {
            for (ProjectLogEntity pl : projectLogEntities) {
                ProjectLogGetDto projectLogDto = new ProjectLogGetDto();
                projectLogDto.setId(pl.getId());
                projectLogDto.setCreationDate(pl.getCreationDate());
                projectLogDto.setContent(pl.getContent());
                projectLogDto.setType(pl.getType());
                projectLogDto.setUsername(pl.getUser().getUsername());
                projectLogDto.setProjectName(pl.getProject().getName());
                projectLogGetDtos.add(projectLogDto);
            }
            LOGGER.info("Successfully fetched {} project logs for project ID: {}", projectLogGetDtos.size(), projectId);
            return projectLogGetDtos;
        } catch (Exception e) {
            LOGGER.error("Error fetching project logs for project ID: {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Error fetching project logs", e);
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Approves or rejects a project based on the provided approval DTO.
     * <p>
     * This method retrieves the project and authenticated user entities, validates the current state
     * of the project, handles the approval or rejection decision, and updates the project state accordingly.
     * A project log entry is created to document the decision, and notifications are sent to all project members.
     * </p>
     *
     * @param projectApproveDto the DTO containing the project approval details.
     * @param securityContext the security context of the authenticated user.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws UnknownHostException if there is an error with the notification service.
     */
    public void approveProject(ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException, UnknownHostException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectApproveDto.getProjectId());
        if (projectEntity == null) {
            LOGGER.warn("Project with ID: {} not found", projectApproveDto.getProjectId());
            throw new EntityNotFoundException("Project with this ID not found");
        }
        // Retrieve the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            LOGGER.warn("User not found with ID: {}", authUserDto.getUserId());
            throw new EntityNotFoundException("User not found");
        }
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState != ProjectStateEnum.READY) {
            LOGGER.info("Project with ID: {} is not in READY state", projectApproveDto.getProjectId());
            throw new IllegalStateException("Project is not in READY state");
        }
        try {
            String comment;
            if (projectApproveDto.isConfirm()) {
                projectEntity.setApproved(true);
                projectEntity.setState(ProjectStateEnum.IN_PROGRESS);
                projectEntity.setInitialDate(Instant.now());
                comment = "Approved with justification: " + projectApproveDto.getComment();
                LOGGER.info("Project with ID: {} approved by user: {}", projectApproveDto.getProjectId(), userEntity.getUsername());
            } else {
                projectEntity.setState(ProjectStateEnum.PLANNING);
                comment = "Rejected with justification: " + projectApproveDto.getComment();
                LOGGER.info("Project with ID: {} rejected by user: {}", projectApproveDto.getProjectId(), userEntity.getUsername());
            }
            createProjectLog(projectEntity, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, comment);
            notificationBean.createNotificationProjectApprovalSendAllMembers(projectEntity, userEntity, projectApproveDto.isConfirm());
        } catch (Exception e) {
            LOGGER.error("Error approving project with ID: {}: {}", projectApproveDto.getProjectId(), e.getMessage());
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Retrieves a paginated list of filtered projects based on the given page number, page size, and filter criteria.
     * <p>
     * This method validates the pagination parameters, retrieves the filtered projects from the database,
     * converts them to DTOs, and returns a paginated response containing the project DTOs and total project count.
     * </p>
     *
     * @param page the page number to retrieve, must be greater than 0.
     * @param pageSize the number of projects per page, must be greater than 0.
     * @param uriInfo the filter criteria for retrieving the projects.
     * @return a ProjectPaginatedDto containing the list of project DTOs and the total project count.
     * @throws InputValidationException if the page or page size is invalid.
     */
    public ProjectPaginatedDto getFilteredProjects(int page, int pageSize, UriInfo uriInfo) throws InputValidationException {
        if (page <= 0) {
            LOGGER.warn("Invalid page number: {}", page);
            throw new InputValidationException("Page must be greater than 0.");
        }
        if (pageSize <= 0) {
            LOGGER.warn("Invalid page size: {}", pageSize);
            throw new InputValidationException("Page size must be greater than 0.");
        }
        try {
            List<ProjectEntity> projectEntities = projectDao.findFilteredProjects(page, pageSize, uriInfo);
            long totalProjects = projectDao.countFilteredProjects(uriInfo);
            List<ProjectGetDto> projectGetDtos = convertProjectEntityListToProjectDtoList(new ArrayList<>(projectEntities));
            LOGGER.info("Successfully retrieved {} projects for page: {}", projectGetDtos.size(), page);
            return new ProjectPaginatedDto(projectGetDtos, totalProjects);
        } catch (Exception e) {
            LOGGER.error("Error retrieving filtered projects for page: {}: {}", page, e.getMessage());
            throw e;
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Updates the role of a project member.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Validates the existence of the user, project, and project membership entities.</li>
     *     <li>Checks that the project creator's role is not being changed.</li>
     *     <li>Updates the role of the project member and logs the change.</li>
     * </ul>
     * </p>
     *
     * @param projectId the ID of the project.
     * @param projectRoleUpdateDto the DTO containing the user ID and new role.
     * @param securityContext the security context containing the authenticated user's details.
     * @throws EntityNotFoundException if the user, project, or project membership is not found.
     * @throws InputValidationException if the project creator's role is being changed.
     * @throws DatabaseOperationException if there is an error updating the project membership role in the database.
     */
    @Transactional
    public void updateProjectMembershipRole(long projectId, ProjectRoleUpdateDto projectRoleUpdateDto, SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, DatabaseOperationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        if (authUserEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        ProjectMembershipEntity projectMembershipEntity = projectMemberDao.findProjectMembershipByUserIdAndProjectId(projectId, projectRoleUpdateDto.getUserId());
        if (projectMembershipEntity == null) {
            throw new EntityNotFoundException("Project Membership not found");
        }
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with ID: " + projectId);
        }
        UserEntity userEntity = userDao.findUserById(projectRoleUpdateDto.getUserId());
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found with ID: " + projectRoleUpdateDto.getUserId());
        }
        if (projectEntity.getCreatedBy().getId() == userEntity.getId()) {
            throw new InputValidationException("Cannot change role of project creator");
        }
        try {
            projectMembershipEntity.setRole(projectRoleUpdateDto.getNewRole());
            projectMemberDao.merge(projectMembershipEntity);
            String content = "User " + userEntity.getUsername() + " has new project role: " + projectRoleUpdateDto.getNewRole();
            createProjectLog(projectEntity, authUserEntity, LogTypeEnum.PROJECT_MEMBERS, content);
        } catch (PersistenceException e) {
            LOGGER.error("Error updating project membership role: {}", e.getMessage());
            throw new DatabaseOperationException("Error updating project membership role");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Updates the details of an existing project.
     * <p>
     * This method performs several validation checks, updates project details, handles state transitions,
     * and logs changes. It performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user from the security context.</li>
     *     <li>Finds the project entity by its ID and validates its existence.</li>
     *     <li>Ensures that cancelled or finished projects are not updated.</li>
     *     <li>Checks for duplicate project names when updating the project's name.</li>
     *     <li>Validates the provided conclusion date to ensure it is not in the past.</li>
     *     <li>Finds the associated laboratory by its ID and validates its existence.</li>
     *     <li>Saves the original state of the project for comparison.</li>
     *     <li>Updates the project fields with the new data from the DTO.</li>
     *     <li>Handles state transitions and sends notifications if necessary.</li>
     *     <li>Compares the original and new project states and logs the changes.</li>
     * </ul>
     * </p>
     *
     * @param projectId The ID of the project to be updated.
     * @param projectUpdateDto The DTO containing the updated project details.
     * @param securityContext The security context containing the authenticated user's details.
     * @throws EntityNotFoundException if the project or user is not found.
     * @throws InputValidationException if the project name is duplicated, the conclusion date is in the past, or there is an invalid state transition.
     * @throws UnknownHostException if there is an error with the host.
     */
    @Transactional
    public void updateProject(long projectId, ProjectUpdateDto projectUpdateDto, SecurityContext securityContext) throws EntityNotFoundException, InputValidationException, UnknownHostException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new EntityNotFoundException("User not found");
        }
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with ID: " + projectId);
        }
        // Don't update CANCELLED or FINISHED projects
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState == ProjectStateEnum.CANCELLED || currentState == ProjectStateEnum.FINISHED) {
            return;
        }
        // When updates project name, check for duplicates
        if (!projectEntity.getName().equalsIgnoreCase(projectUpdateDto.getName())) {
            if (projectDao.checkProjectNameExist(projectUpdateDto.getName())) {
                throw new InputValidationException("Duplicated project name");
            }
        }
        if (projectUpdateDto.getConclusionDate() != null && projectUpdateDto.getConclusionDate().isBefore(Instant.now())) {
            //make the concluision date the day after the present day
            projectUpdateDto.setConclusionDate(Instant.now().plus(1, ChronoUnit.DAYS));
        }
        // Find associated laboratory
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectUpdateDto.getLaboratoryId());
        if (laboratoryEntity == null) {
            throw new EntityNotFoundException("Laboratory not found with ID: " + projectUpdateDto.getLaboratoryId());
        }
        // Save the original state of the project
        ProjectEntity originalProject = copyProjectEntity(projectEntity);
        // Update fields
        projectEntity.setName(projectUpdateDto.getName());
        projectEntity.setDescription(projectUpdateDto.getDescription());
        projectEntity.setMotivation(projectUpdateDto.getMotivation());
        projectEntity.setConclusionDate(projectUpdateDto.getConclusionDate());
        projectEntity.setLaboratory(laboratoryEntity);

        ProjectStateEnum newState = projectUpdateDto.getState();

        //Creating Notification for allsystem Admins that the project s now on approval mode
        if (newState == ProjectStateEnum.READY && currentState == ProjectStateEnum.PLANNING) {
            notificationBean.createNotificationForAllPlatformAdminsProjectApproval(projectEntity);
        }
        // Validate state and handle state transitions
        boolean approved = projectEntity.isApproved();
        if (currentState != newState) {
            if (!approved) {
                if (newState == ProjectStateEnum.PLANNING || newState == ProjectStateEnum.READY || newState == ProjectStateEnum.CANCELLED) {
                    projectEntity.setState(newState);
                } else {
                    throw new InputValidationException("Invalid state transition: Project is not approved");
                }
            } else {
                if (newState == ProjectStateEnum.FINISHED || newState == ProjectStateEnum.CANCELLED) {
                    projectEntity.setState(newState);
                    projectEntity.setFinalDate(Instant.now());
                } else {
                    throw new InputValidationException("Invalid state transition: Project is approved");
                }
            }
        }
        // Compare old and new project states and create project logs
        compareAndLogChanges(originalProject, projectEntity, userEntity);
    }


    /**
     * Creates a copy of the given ProjectEntity.
     * <p>
     * This method creates a new instance of ProjectEntity and copies the relevant fields from the provided project entity.
     * The copied fields include name, description, motivation, conclusion date, associated laboratory, and the current state of the project.
     * </p>
     *
     * @param projectEntity The original ProjectEntity to be copied.
     * @return A new ProjectEntity instance with the same field values as the provided projectEntity.
     */
    private ProjectEntity copyProjectEntity(ProjectEntity projectEntity) {
        ProjectEntity originalProject = new ProjectEntity();
        originalProject.setName(projectEntity.getName());
        originalProject.setDescription(projectEntity.getDescription());
        originalProject.setMotivation(projectEntity.getMotivation());
        originalProject.setConclusionDate(projectEntity.getConclusionDate());
        originalProject.setLaboratory(projectEntity.getLaboratory());
        originalProject.setState(projectEntity.getState());
        return originalProject;
    }


    /**
     * Compares the attributes of the old and new project entities and logs any changes.
     * <p>
     * This method compares the name, description, motivation, conclusion date, laboratory, and state of the provided
     * old and new ProjectEntity instances. If any of these attributes have changed, it creates a log entry detailing
     * the change.
     * </p>
     *
     * @param oldProject The original ProjectEntity before updates.
     * @param newProject The updated ProjectEntity after changes.
     * @param userEntity The UserEntity responsible for making the changes.
     */
    private void compareAndLogChanges(ProjectEntity oldProject, ProjectEntity newProject, UserEntity userEntity) {
        if (!Objects.equals(oldProject.getName(), newProject.getName())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "Name", oldProject.getName(), newProject.getName());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
        if (!Objects.equals(oldProject.getDescription(), newProject.getDescription())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "Description", oldProject.getDescription(), newProject.getDescription());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
        if (!Objects.equals(oldProject.getMotivation(), newProject.getMotivation())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "Motivation", oldProject.getMotivation(), newProject.getMotivation());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
        if (!Objects.equals(oldProject.getConclusionDate(), newProject.getConclusionDate())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "Conclusion Date", oldProject.getConclusionDate().toString(), newProject.getConclusionDate().toString());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
        if (!Objects.equals(oldProject.getLaboratory().getId(), newProject.getLaboratory().getId())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "Laboratory", oldProject.getLaboratory().getLocation(), newProject.getLaboratory().getLocation());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
        if (!Objects.equals(oldProject.getState(), newProject.getState())) {
            String logContent = String.format("Attribute '%s' changed from '%s' to '%s'", "State", oldProject.getState(), newProject.getState());
            createProjectLog(newProject, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, logContent);
        }
    }

    /**
     * Retrieves a list of all project IDs in the system.
     * <p>
     * This method queries the database to get a list of all project IDs. It leverages the projectDao to perform
     * the database operation.
     * </p>
     *
     * @return A list of Long values representing the IDs of all projects.
     */
    public List<Long> getAllProjectsIds() {
        return projectDao.getAllProjectsIds();
    }

    @Transactional
    public void createProjectLog(ProjectEntity projectEntity, UserEntity userEntity, LogTypeEnum type, String content)  {
        try{
            ProjectLogEntity projectLogEntity = new ProjectLogEntity();
            projectLogEntity.setProject(projectEntity);
            projectLogEntity.setUser(userEntity);
            projectLogEntity.setCreationDate(Instant.now());
            projectLogEntity.setType(type);
            projectLogEntity.setContent(content);
            projectLogDao.persist(projectLogEntity);
            projectEntity.getProjectLogs().add(projectLogEntity);
            LOGGER.info("Project log created successfully: {}", content);
        } catch (PersistenceException e) {
            LOGGER.error("Error creating project log: {}", e.getMessage());
            throw new PersistenceException("Error creating project log");
        } finally {
            ThreadContext.clearMap();
        }
    }


    /**
     * Converts a ProjectEntity object to a ProjectGetDto object.
     * <p>
     * This method creates a new ProjectGetDto and populates its fields with data from the given ProjectEntity.
     * It converts fields such as ID, name, description, motivation, state, dates, approval status,
     * associated laboratory, creator information, and project members.
     * </p>
     *
     * @param projectEntity The ProjectEntity object to convert.
     * @return A ProjectGetDto object populated with data from the ProjectEntity.
     */
    public ProjectGetDto convertProjectEntityToProjectDto(ProjectEntity projectEntity) {
        ProjectGetDto projectGetDto = new ProjectGetDto();
        projectGetDto.setId(projectEntity.getId());
        projectGetDto.setName(projectEntity.getName());
        projectGetDto.setDescription(projectEntity.getDescription());
        projectGetDto.setMotivation(projectEntity.getMotivation());
        projectGetDto.setState(projectEntity.getState());
        projectGetDto.setCreationDate(projectEntity.getCreationDate());
        projectGetDto.setInitialDate(projectEntity.getInitialDate());
        projectGetDto.setFinalDate(projectEntity.getFinalDate());
        projectGetDto.setConclusionDate(projectEntity.getConclusionDate());
        projectGetDto.setApproved(projectEntity.isApproved());
        projectGetDto.setLaboratory(laboratoryBean.convertLaboratoryEntityToLaboratoryDto(projectEntity.getLaboratory()));
        projectGetDto.setCreatedBy(userBean.convertUserEntitytoUserBasicInfoDto(projectEntity.getCreatedBy()));
        projectGetDto.setMembers(convertProjectMembershipEntityListToDto(projectEntity.getMembers()));
        return projectGetDto;
    }


    /**
     * Converts a list of ProjectEntity objects to a list of ProjectGetDto objects.
     * <p>
     * This method iterates over the provided list of ProjectEntity objects, converting each
     * ProjectEntity to a ProjectGetDto using {@link #convertProjectEntityToProjectDto(ProjectEntity)}.
     * It returns an ArrayList containing all converted ProjectGetDto objects.
     * </p>
     *
     * @param projectEntities The ArrayList of ProjectEntity objects to convert.
     * @return An ArrayList of ProjectGetDto objects populated with data from the ProjectEntity objects.
     */
    public ArrayList<ProjectGetDto> convertProjectEntityListToProjectDtoList(ArrayList<ProjectEntity> projectEntities) {
        ArrayList<ProjectGetDto> projectGetDtos = new ArrayList<>();
        for (ProjectEntity p : projectEntities) {
            ProjectGetDto projectGetDto = convertProjectEntityToProjectDto(p);
            projectGetDtos.add(projectGetDto);
        }
        return projectGetDtos;
    }


    /**
     * Converts a ProjectMembershipEntity object to a ProjectMembershipDto object.
     * <p>
     * This method creates a new ProjectMembershipDto instance and populates it with data
     * from the provided ProjectMembershipEntity. It sets the id, userId, projectId, role,
     * accepted status, and user information by converting the associated UserEntity to
     * a UserBasicInfoDto using {@link UserBean#convertUserEntitytoUserBasicInfoDto(UserEntity)}.
     * </p>
     *
     * @param projectMembershipEntity The ProjectMembershipEntity object to convert.
     * @return A ProjectMembershipDto object populated with data from the ProjectMembershipEntity.
     */
    public ProjectMembershipDto convertProjectMembershipEntityToDto(ProjectMembershipEntity projectMembershipEntity) {
        ProjectMembershipDto projectMembershipDto = new ProjectMembershipDto();
        projectMembershipDto.setId(projectMembershipEntity.getId());
        projectMembershipDto.setUserId(projectMembershipEntity.getUser().getId());
        projectMembershipDto.setProjectId(projectMembershipEntity.getProject().getId());
        projectMembershipDto.setRole(projectMembershipEntity.getRole());
        projectMembershipDto.setAccepted(projectMembershipEntity.isAccepted());
        projectMembershipDto.setUser(userBean.convertUserEntitytoUserBasicInfoDto(projectMembershipEntity.getUser()));
        return projectMembershipDto;
    }


    /**
     * Converts a Set of ProjectMembershipEntity objects to an ArrayList of ProjectMembershipDto objects.
     * <p>
     * This method iterates through each ProjectMembershipEntity in the provided set and converts it
     * to a ProjectMembershipDto using {@link #convertProjectMembershipEntityToDto(ProjectMembershipEntity)}.
     * It then adds each converted ProjectMembershipDto to an ArrayList, which is returned as the result.
     * </p>
     *
     * @param projectMemberships The Set of ProjectMembershipEntity objects to convert.
     * @return An ArrayList of ProjectMembershipDto objects populated with data from the ProjectMembershipEntity set.
     */
    public ArrayList<ProjectMembershipDto> convertProjectMembershipEntityListToDto(Set<ProjectMembershipEntity> projectMemberships) {
        ArrayList<ProjectMembershipDto> projectMembershipDtos = new ArrayList<>();
        for (ProjectMembershipEntity p : projectMemberships) {
            ProjectMembershipDto projectMembershipDto = convertProjectMembershipEntityToDto(p);
            projectMembershipDtos.add(projectMembershipDto);
        }
        return projectMembershipDtos;
    }
}
