package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.*;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Stateless
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(ProjectBean.class);

    @EJB
    ProjectDao projectDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    TaskDao taskDao;
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
    EmailService emailService;


    @Transactional
    public void createProject(ProjectCreateDto projectCreateDto, SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException, InputValidationException, UserNotFoundException, ElementAssociationException {
        if (projectCreateDto.getConclusionDate() != null && projectCreateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new InputValidationException("Conclusion date cannot be in the past");
        }
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity user = userDao.findUserById(authUserDto.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User with Id: " + authUserDto.getUserId() + " not found");
        }
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectCreateDto.getLaboratoryId());
        if (laboratoryEntity == null) {
            throw new EntityNotFoundException("Lab with Id: " + projectCreateDto.getLaboratoryId() + " not found");
        }
        // Check for duplicate project name
        if (projectDao.checkProjectNameExist(projectCreateDto.getName())) {
            throw new InputValidationException("Duplicated project name");
        }
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectCreateDto.getName());
        projectEntity.setDescription(projectCreateDto.getDescription());
        projectEntity.setMotivation(projectCreateDto.getMotivation());
        projectEntity.setConclusionDate(projectCreateDto.getConclusionDate());
        projectEntity.setLaboratory(laboratoryEntity);
        projectEntity.setCreatedBy(user);
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setCreationDate(Instant.now());
        projectEntity.setApproved(false);
        projectDao.persist(projectEntity);
        // Define relations on the persisted Project
        ProjectEntity persistedProject = projectDao.findProjectByName(projectEntity.getName());
        String content = "Creation of " + projectEntity.getName();
        createProjectLog(projectEntity, user, LogTypeEnum.GENERAL_PROJECT_DATA, content);
        addRelationsToProject(projectCreateDto, persistedProject, user);
    }

    private void addRelationsToProject(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity, UserEntity userCreator) throws EntityNotFoundException, DuplicatedAttributeException, UserNotFoundException, InputValidationException, ElementAssociationException {
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

    public ArrayList<ProjectGetDto> getAllProjects() {
        ArrayList<ProjectEntity> projects = projectDao.findAllProjects();
        if (projects != null && !projects.isEmpty()) {
            return convertProjectEntityListToProjectDtoList(projects);
        } else {
            return new ArrayList<>();
        }
    }

    public ProjectGetDto getProjectDetailsById(long projectId) throws EntityNotFoundException {
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity != null) {
            return convertProjectEntityToProjectDto(projectEntity);
        } else {
            throw new EntityNotFoundException("Project not found");
        }
    }

    public List<ProjectStateEnum> getEnumListProjectStates() {
        List<ProjectStateEnum> projectStateEnums = new ArrayList<>();
        for (ProjectStateEnum projectStateEnum : ProjectStateEnum.values()) {
            projectStateEnums.add(projectStateEnum);
        }
        return projectStateEnums;
    }

    public List<ProjectRoleEnum> getEnumListProjectRoles() {
        List<ProjectRoleEnum> projectRoleEnums = new ArrayList<>();
        for (ProjectRoleEnum projectRoleEnum : ProjectRoleEnum.values()) {
            projectRoleEnums.add(projectRoleEnum);
        }
        return projectRoleEnums;
    }

    public List<ProjectLogGetDto> getListProjectLogs(long projectId) {
        List<ProjectLogEntity> projectLogEntities = projectLogDao.findProjectLogsByProjectId(projectId);
        List<ProjectLogGetDto> projectLogGetDtos = new ArrayList<>();
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
        return projectLogGetDtos;
    }

    public void approveProject(ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException {
        // Retrieve the project entity
        ProjectEntity projectEntity = projectDao.findProjectById(projectApproveDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project with this Id not found");
        }
        // Retrieve the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        // Validate project state
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState != ProjectStateEnum.READY) {
            return;
        }
        String comment = "";
        // Handle admin decision and state transitions
        if (projectApproveDto.isConfirm()) {
            projectEntity.setApproved(true);
            projectEntity.setState(ProjectStateEnum.IN_PROGRESS);
            projectEntity.setInitialDate(Instant.now());
            comment = "Approved with justification: " + projectApproveDto.getComment();
        } else {
            projectEntity.setState(ProjectStateEnum.PLANNING);
            comment = "Rejected with justification: " + projectApproveDto.getComment();
        }
        // Create a Project Log
        createProjectLog(projectEntity, userEntity, LogTypeEnum.GENERAL_PROJECT_DATA, comment);
    }

    public ProjectPaginatedDto getFilteredProjects(int page, int pageSize, UriInfo uriInfo) throws InputValidationException {
        if (page <= 0) {
            throw new InputValidationException("Page must be greater than 0.");
        }
        if (pageSize <= 0) {
            throw new InputValidationException("Page size must be greater than 0.");
        }
        List<ProjectEntity> projectEntities = projectDao.findFilteredProjects(page, pageSize, uriInfo);
        long totalProjects = projectDao.countFilteredProjects(uriInfo);
        List<ProjectGetDto> projectGetDtos = convertProjectEntityListToProjectDtoList(new ArrayList<>(projectEntities));
        return new ProjectPaginatedDto(projectGetDtos, totalProjects);
    }

    public void updateProjectMembershipRole(long projectId, ProjectRoleUpdateDto projectRoleUpdateDto, SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        ProjectMembershipEntity projectMembershipEntity = projectMemberDao.findProjectMembershipByUserIdAndProjectId(projectId, projectRoleUpdateDto.getUserId());
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        UserEntity userEntity = userDao.findUserById(projectRoleUpdateDto.getUserId());
        // Check if user is project creator
        if (projectEntity.getCreatedBy().getId() == userEntity.getId()) {
            throw new InputValidationException("Cannot change role of project creator");
        }
        if (projectMembershipEntity != null) {
            projectMembershipEntity.setRole(projectRoleUpdateDto.getNewRole());
            projectMemberDao.merge(projectMembershipEntity);
        } else {
            throw new EntityNotFoundException("Project Membership not found");
        }
        String content = "User " + userEntity.getUsername() + " has new project role: " + projectRoleUpdateDto.getNewRole();
        createProjectLog(projectEntity, authUserEntity, LogTypeEnum.PROJECT_MEMBERS, content);
    }

    @Transactional
    public void updateProject(long projectId, ProjectUpdateDto projectUpdateDto, SecurityContext securityContext) throws EntityNotFoundException, InputValidationException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        // Find existing project
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
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
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
        // Validate state and handle state transitions
        ProjectStateEnum newState = projectUpdateDto.getState();
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
    public List<Long> getAllProjectsIds() {
        return projectDao.getAllProjectsIds();
    }

    // Create a Project Log
    public void createProjectLog(ProjectEntity projectEntity, UserEntity userEntity, LogTypeEnum type, String content) {
        ProjectLogEntity projectLogEntity = new ProjectLogEntity();
        projectLogEntity.setProject(projectEntity);
        projectLogEntity.setUser(userEntity);
        projectLogEntity.setCreationDate(Instant.now());
        projectLogEntity.setType(type);
        projectLogEntity.setContent(content);
        projectLogDao.persist(projectLogEntity);
        projectEntity.getProjectLogs().add(projectLogEntity);
    }

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
        projectGetDto.setCreatedBy(userBean.convertUserEntetyToUserBasicInfoDto(projectEntity.getCreatedBy()));
        projectGetDto.setMembers(convertProjectMembershipEntityListToDto(projectEntity.getMembers()));
        return projectGetDto;
    }

    public ArrayList<ProjectGetDto> convertProjectEntityListToProjectDtoList(ArrayList<ProjectEntity> projectEntities) {
        ArrayList<ProjectGetDto> projectGetDtos = new ArrayList<>();
        for (ProjectEntity p : projectEntities) {
            ProjectGetDto projectGetDto = convertProjectEntityToProjectDto(p);
            projectGetDtos.add(projectGetDto);
        }
        return projectGetDtos;
    }

    public ProjectMembershipDto convertProjectMembershipEntityToDto(ProjectMembershipEntity projectMembershipEntity) {
        ProjectMembershipDto projectMembershipDto = new ProjectMembershipDto();
        projectMembershipDto.setId(projectMembershipEntity.getId());
        projectMembershipDto.setUserId(projectMembershipEntity.getUser().getId());
        projectMembershipDto.setProjectId(projectMembershipEntity.getProject().getId());
        projectMembershipDto.setRole(projectMembershipEntity.getRole());
        projectMembershipDto.setAccepted(projectMembershipEntity.isAccepted());
        projectMembershipDto.setUser(userBean.convertUserEntetyToUserBasicInfoDto(projectMembershipEntity.getUser()));
        return projectMembershipDto;
    }

    public ArrayList<ProjectMembershipDto> convertProjectMembershipEntityListToDto(Set<ProjectMembershipEntity> projectMemberships) {
        ArrayList<ProjectMembershipDto> projectMembershipDtos = new ArrayList<>();
        for (ProjectMembershipEntity p : projectMemberships) {
            ProjectMembershipDto projectMembershipDto = convertProjectMembershipEntityToDto(p);
            projectMembershipDtos.add(projectMembershipDto);
        }
        return projectMembershipDtos;
    }

}
