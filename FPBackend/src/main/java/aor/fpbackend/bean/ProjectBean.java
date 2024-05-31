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
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
    public boolean createProject(ProjectCreateDto projectCreateDto, SecurityContext securityContext) throws EntityNotFoundException, AttributeAlreadyExistsException, InputValidationException {
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
        addRelationsToProject(projectCreateDto, persistedProject);

        return true;
    }

    private void addRelationsToProject(ProjectCreateDto projectCreateDto, ProjectEntity projectEntity) throws EntityNotFoundException, AttributeAlreadyExistsException {
        // Define relations for project members (Users)
        if (projectCreateDto.getUsers() != null && !projectCreateDto.getUsers().isEmpty()) {
            Set<String> usernames = projectCreateDto.getUsers().stream().map(UsernameDto::getUsername).collect(Collectors.toSet());
            for (String username : usernames) {
                userBean.addUserToProject(username, projectEntity.getId());
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
        TaskEntity defaultTask = new TaskEntity("Final Presentation", Instant.now(), 1,
                TaskStateEnum.PLANNED, projectEntity);
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

//    public ProjectGetDto getProjectDetailsById(long projectId) throws EntityNotFoundException {
//        try {
//            ProjectEntity projectEntity = projectDao.findProjectById(projectId);
//            if (projectEntity != null) {
//                return convertProjectEntityToProjectDto(projectEntity);
//            } else {
//                throw new EntityNotFoundException("Project not found");
//            }
//        } catch (EntityNotFoundException e) {
//            throw new EntityNotFoundException("Project not found");
//        }
//    }

    public void sendInviteToUser(ProjectInviteDto projectInviteDto) throws UserNotFoundException, InputValidationException {
        if (projectInviteDto == null) {
            throw new InputValidationException("Invalid Dto");
        }
            UserEntity user = userDao.findUserByUsername(projectInviteDto.getUsername());
            if (user == null) {
                LOGGER.warn("Attempt to send invitation to project at - User not found: " + projectInviteDto.getUsername());
                throw new UserNotFoundException("Not found user with this username");
            }
            ProjectEntity projectEntity = projectDao.findProjectById(projectInviteDto.getProjectId());
        if (projectEntity == null) {
            throw new InputValidationException("Project not found");
        }
            String acceptanceToken = userBean.generateNewToken();
            ProjectMembershipEntity pendingInvite = new ProjectMembershipEntity();
            pendingInvite.setAcceptanceToken(acceptanceToken);
            pendingInvite.setProject(projectEntity);
            pendingInvite.setUser(user);
            pendingInvite.setRole(ProjectRoleEnum.NORMAL_USER);
            pendingInvite.setAccepted(false);
            projectMemberDao.persist(pendingInvite);
            emailService.sendInvitationToProjectEmail(user.getEmail(), acceptanceToken, projectEntity.getName());
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
        projectMembershipDto.setAcceptanceToken(projectMembershipEntity.getAcceptanceToken());
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
