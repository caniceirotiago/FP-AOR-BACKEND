package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.*;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.jws.soap.SOAPBinding;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
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


    @Transactional
    public boolean createProject(ProjectCreateDto projectCreateDto, SecurityContext securityContext) throws EntityNotFoundException, AttributeAlreadyExistsException, InputValidationException, UserNotFoundException {
        if (projectCreateDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
        if (projectCreateDto.getLaboratoryId() <= 0) {
            throw new IllegalArgumentException("Laboratory ID must be a positive number");
        }
        if (projectCreateDto.getMotivation() != null && projectCreateDto.getMotivation().isEmpty()) {
            throw new IllegalArgumentException("Motivation, if provided, cannot be empty");
        }
        if (projectCreateDto.getConclusionDate() != null && projectCreateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
        }

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectCreateDto.getName());
        projectEntity.setDescription(projectCreateDto.getDescription());
        projectEntity.setMotivation(projectCreateDto.getMotivation());
        projectEntity.setConclusionDate(projectCreateDto.getConclusionDate());
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectCreateDto.getLaboratoryId());
        projectEntity.setLaboratory(laboratoryEntity);
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity user = userDao.findUserById(authUserDto.getUserId());
        projectEntity.setCreatedBy(user);
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setCreationDate(Instant.now());
        projectDao.persist(projectEntity);

        ProjectEntity persistedProject = projectDao.findProjectByName(projectEntity.getName());
        System.out.println(persistedProject);
        addRelationsToProject(projectCreateDto, persistedProject, user);

        return true;
    }

    private void addRelationsToProject(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity, UserEntity userCreator) throws EntityNotFoundException, AttributeAlreadyExistsException, UserNotFoundException, InputValidationException {
        // Define relations for project members (Users)
        userBean.addUserToProject(userCreator.getUsername(), projectEntity.getId(), true, true);
        if (projectCreateDto.getUsers() != null && !projectCreateDto.getUsers().isEmpty()) {
            Set<String> usernames = projectCreateDto.getUsers().stream().map(UsernameDto::getUsername).collect(Collectors.toSet());
            System.out.println(projectCreateDto.getUsers());
            System.out.println(usernames);
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
                keywordBean.addKeyword(keywordName,  projectEntity.getId());
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
        try {
            ProjectEntity projectEntity = projectDao.findProjectById(projectId);
            if (projectEntity != null) {
                return convertProjectEntityToProjectDto(projectEntity);
            } else {
                throw new EntityNotFoundException("Project not found");
            }
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Project not found");
        }
    }

    public void sendInviteToUser(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) throws UserNotFoundException, InputValidationException {

            emailService.sendInvitationToProjectEmail(user.getEmail(), membershipEntity.getAcceptanceToken(), projectEntity.getName());
    }
    public void sendJoinRequisitionToManagers(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) throws UserNotFoundException, InputValidationException {
        List<UserEntity> projectManagers = projectMemberDao.findProjectManagers(projectEntity.getId());
        System.out.println(projectManagers);
        for (UserEntity manager : projectManagers) {
            System.out.println(manager);
            emailService.sendJoinRequisitionToManagersEmail(manager.getEmail(), user.getUsername(), projectEntity.getName(), membershipEntity.getAcceptanceToken());
        }
    }
    public ProjectsPaginatedDto getFilteredProjects(int page, int pageSize, UriInfo uriInfo) {
        List<ProjectEntity> projectEntities = projectDao.findFilteredProjects(page, pageSize, uriInfo);
        long totalProjects = projectDao.countFilteredProjects(uriInfo);
        List<ProjectGetDto> projectGetDtos = convertProjectEntityListToProjectDtoList(new ArrayList<>(projectEntities));
        return new ProjectsPaginatedDto(projectGetDtos, totalProjects);
    }

    public void updateProjectMembershipRole(ProjectRoleUpdateDto projectRoleUpdateDto) throws EntityNotFoundException, InputValidationException {
        ProjectMembershipEntity projectMembershipEntity = projectMemberDao.findProjectMembershipByUserIdAndProjectID(projectRoleUpdateDto.getProjectId(), projectRoleUpdateDto.getUserId());

        ProjectEntity projectEntity = projectDao.findProjectById(projectRoleUpdateDto.getProjectId());
        UserEntity userEntity = userDao.findUserById(projectRoleUpdateDto.getUserId());
        // Check if user is project creator
        if(projectEntity.getCreatedBy().getId() == userEntity.getId()){
            throw new InputValidationException("Cannot change role of project creator");
        }

        if (projectMembershipEntity != null) {
            projectMembershipEntity.setRole(projectRoleUpdateDto.getRole());
            projectMemberDao.merge(projectMembershipEntity);
        } else {
            throw new EntityNotFoundException("Project Membership not found");
        }
    }
    public void askToJoinProject(ProjectAskJoinDto projectAskJoinDto, SecurityContext securityContext) throws EntityNotFoundException, UserNotFoundException, InputValidationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        ProjectEntity projectEntity = projectDao.findProjectById(projectAskJoinDto.getProjectId());
        System.out.println(projectEntity);
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        System.out.println(userEntity);
        if (projectEntity != null && userEntity != null) {
            ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
            projectMembershipEntity.setProject(projectEntity);
            projectMembershipEntity.setUser(userEntity);
            projectMembershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
            projectMembershipEntity.setAccepted(false);
            projectMembershipEntity.setAcceptanceToken(UUID.randomUUID().toString());
            System.out.println("entrou");
            projectMemberDao.persist(projectMembershipEntity);
            System.out.println("saiu");
            sendJoinRequisitionToManagers(projectMembershipEntity, userEntity, projectEntity);
        } else {
            throw new EntityNotFoundException("Project or User not found");
        }
    }
    @Transactional
    public void updateProject(ProjectUpdateDto projectUpdateDto) throws EntityNotFoundException, InputValidationException {
        // Validate DTO
        if (projectUpdateDto == null) {
            throw new InputValidationException("Invalid DTO");
        }
        if (projectUpdateDto.getLaboratoryId() <= 0) {
            throw new IllegalArgumentException("Laboratory ID must be a positive number");
        }
        if (projectUpdateDto.getMotivation() != null && projectUpdateDto.getMotivation().isEmpty()) {
            throw new IllegalArgumentException("Motivation, if provided, cannot be empty");
        }
        if (projectUpdateDto.getConclusionDate() != null && projectUpdateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
        }

        // Find existing project
        ProjectEntity projectEntity = projectDao.findProjectById(projectUpdateDto.getId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found with ID: " + projectUpdateDto.getId());
        }

        // Update fields
        projectEntity.setName(projectUpdateDto.getName());
        projectEntity.setDescription(projectUpdateDto.getDescription());
        projectEntity.setMotivation(projectUpdateDto.getMotivation());
        projectEntity.setState(projectUpdateDto.getState());
        projectEntity.setConclusionDate(projectUpdateDto.getConclusionDate());

        // Update laboratory
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectUpdateDto.getLaboratoryId());
        if (laboratoryEntity == null) {
            throw new EntityNotFoundException("Laboratory not found with ID: " + projectUpdateDto.getLaboratoryId());
        }
        projectEntity.setLaboratory(laboratoryEntity);

        // Persist changes
        projectDao.merge(projectEntity);
    }


    private ProjectEntity convertProjectDtotoProjectEntity(ProjectCreateDto projectCreateDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectCreateDto.getName());
        projectEntity.setDescription(projectCreateDto.getDescription());
        projectEntity.setMotivation(projectCreateDto.getMotivation());
        projectEntity.setConclusionDate(projectCreateDto.getConclusionDate());
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectCreateDto.getLaboratoryId());
        projectEntity.setLaboratory(laboratoryEntity);
        projectEntity.setState(ProjectStateEnum.PLANNING);

        return projectEntity;
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
