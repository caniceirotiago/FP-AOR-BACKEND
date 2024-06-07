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
    UserBean userBean;
    @EJB
    SkillBean skillBean;
    @EJB
    KeywordBean keywordBean;
    @EJB
    TaskDao taskDao;
    @EJB
    EmailService emailService;
    @EJB
    UserDao userDao;
    @EJB
    ProjectMembershipDao projectMemberDao;
    @EJB
    LaboratoryBean laboratoryBean;

    @EJB
    ProjectLogDao projectLogDao;


    @Transactional
    public void createProject(ProjectCreateDto projectCreateDto, SecurityContext securityContext) throws EntityNotFoundException, AttributeAlreadyExistsException, InputValidationException, UserNotFoundException {
        if (projectCreateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        if (projectCreateDto.getConclusionDate() != null && projectCreateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
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
        addRelationsToProject(projectCreateDto, persistedProject, user);
    }

    private void addRelationsToProject(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity, UserEntity userCreator) throws EntityNotFoundException, AttributeAlreadyExistsException, UserNotFoundException, InputValidationException {
        // Define relations for project members (Users)
        userBean.addUserToProject(userCreator.getUsername(), projectEntity.getId(), true, true);
        if (projectCreateDto.getUsers() != null && !projectCreateDto.getUsers().isEmpty()) {
            Set<String> usernames = projectCreateDto.getUsers().stream().map(UsernameDto::getUsername).collect(Collectors.toSet());
            // Add creator to project
            for (String username : usernames) {
                userBean.addUserToProject(username, projectEntity.getId(), true, false);
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
        // Define default final Task
        TaskEntity defaultTask = new TaskEntity("Final Presentation nº" + projectEntity.getId(), "Presentation of project: " + projectEntity.getName(),
                Instant.now(), 1, TaskStateEnum.PLANNED, projectEntity, userCreator);
        taskDao.persist(defaultTask);
        Set<TaskEntity> projectTasks = new HashSet<>();
        projectTasks.add(defaultTask);
        projectEntity.setTasks(projectTasks);
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

    public void approveProject(ProjectApproveDto projectApproveDto, @Context SecurityContext securityContext) throws EntityNotFoundException, UserNotFoundException, InputValidationException {
        // Validate input DTO
        if (projectApproveDto == null) {
            throw new InputValidationException("Invalid DTO");
        }
        // Retrieve the project entity
        ProjectEntity projectEntity = projectDao.findProjectById(projectApproveDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project with this Id not found");
        }
        // Retrieve the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User with this Id not found");
        }
        // Validate project state
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState != ProjectStateEnum.READY) {
            return;
        }
        // Handle admin decision and state transitions
        if (projectApproveDto.isConfirm()) {
            projectEntity.setApproved(true);
            projectEntity.setState(ProjectStateEnum.IN_PROGRESS);
            projectEntity.setInitialDate(Instant.now());
        } else {
            projectEntity.setState(ProjectStateEnum.PLANNING);
        }
        // Create a Project Log
        ProjectLogEntity projectLogEntity = new ProjectLogEntity();
        projectLogEntity.setProject(projectEntity);
        projectLogEntity.setUser(userEntity);
        projectLogEntity.setCreationDate(Instant.now());
        projectLogEntity.setType("Approval");
        projectLogEntity.setContent(projectApproveDto.getComment());
        projectLogDao.persist(projectLogEntity);
        projectEntity.getProjectLogs().add(projectLogEntity);
    }

    //TODO Only Project Members can invite other users!
    public void sendInviteToUser(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        emailService.sendInvitationToProjectEmail(user.getEmail(), membershipEntity.getAcceptanceToken(), projectEntity.getName());
    }

    public void sendJoinRequisitionToManagers(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        List<UserEntity> projectManagers = projectMemberDao.findProjectManagers(projectEntity.getId());
        for (UserEntity manager : projectManagers) {
            emailService.sendJoinRequisitionToManagersEmail(manager.getEmail(), user.getUsername(), projectEntity.getName(), membershipEntity.getAcceptanceToken());
        }
    }

    public ProjectsPaginatedDto getFilteredProjects(int page, int pageSize, UriInfo uriInfo) {
        List<ProjectEntity> projectEntities = projectDao.findFilteredProjects(page, pageSize, uriInfo);
        long totalProjects = projectDao.countFilteredProjects(uriInfo);
        List<ProjectGetDto> projectGetDtos = convertProjectEntityListToProjectDtoList(new ArrayList<>(projectEntities));
        return new ProjectsPaginatedDto(projectGetDtos, totalProjects);
    }

    public void updateProjectMembershipRole(long projectId, ProjectRoleUpdateDto projectRoleUpdateDto) throws EntityNotFoundException, InputValidationException {
        if (projectRoleUpdateDto == null) {
            throw new InputValidationException("Invalid DTO");
        }
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
    }

    public void askToJoinProject(ProjectAskJoinDto projectAskJoinDto, SecurityContext securityContext) throws EntityNotFoundException, UserNotFoundException, InputValidationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        ProjectEntity projectEntity = projectDao.findProjectById(projectAskJoinDto.getProjectId());
        if (projectEntity != null && userEntity != null) {
            ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
            projectMembershipEntity.setProject(projectEntity);
            projectMembershipEntity.setUser(userEntity);
            projectMembershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
            projectMembershipEntity.setAccepted(false);
            projectMembershipEntity.setAcceptanceToken(UUID.randomUUID().toString());
            projectMemberDao.persist(projectMembershipEntity);
            sendJoinRequisitionToManagers(projectMembershipEntity, userEntity, projectEntity);
        } else {
            throw new EntityNotFoundException("Project or User not found");
        }
    }

    @Transactional
    public void updateProject(long projectId, ProjectUpdateDto projectUpdateDto) throws EntityNotFoundException, InputValidationException {
        // Validate DTO
        if (projectUpdateDto == null) {
            throw new InputValidationException("Invalid DTO");
        }
        if (projectUpdateDto.getConclusionDate() != null && projectUpdateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
        }
        // Find associated laboratory
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectUpdateDto.getLaboratoryId());
        if (laboratoryEntity == null) {
            throw new EntityNotFoundException("Laboratory not found with ID: " + projectUpdateDto.getLaboratoryId());
        }
        // Find existing project
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with ID: " + projectId);
        }
        // Check for duplicate project name, if updating the project name
        if (!projectEntity.getName().equals(projectUpdateDto.getName())) {
            if (projectDao.checkProjectNameExist(projectUpdateDto.getName())) {
                throw new InputValidationException("Duplicated project name");
            }
        }
        ProjectStateEnum currentState = projectEntity.getState();
        if (currentState==ProjectStateEnum.CANCELLED){
            return;
        }
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
                } else {
                    throw new InputValidationException("Invalid state transition: Project is approved");
                }
            }
        }
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
        projectGetDto.setMembers(convertProjectMembershipsEntityToDto(projectEntity.getMembers()));
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

    public ArrayList<ProjectMembershipDto> convertProjectMembershipsEntityToDto(Set<ProjectMembershipEntity> projectMemberships) {
        ArrayList<ProjectMembershipDto> projectMembershipDtos = new ArrayList<>();
        for (ProjectMembershipEntity p : projectMemberships) {
            ProjectMembershipDto projectMembershipDto = convertProjectMembershipEntityToDto(p);
            projectMembershipDtos.add(projectMembershipDto);
        }
        return projectMembershipDtos;
    }

}
