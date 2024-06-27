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
import aor.fpbackend.enums.SkillTypeEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
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
public class SkillBean implements Serializable {
    @EJB
    SkillDao skillDao;
    @EJB
    ProjectDao projectDao;
    @EJB
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SkillBean.class);

    @Transactional
    public void addSkillUser(SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException {
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillAddUserDto.getName(), skillAddUserDto.getType());
        // Find the skill by name
        SkillEntity skillEntity = skillDao.findSkillByName(skillAddUserDto.getName());
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        // Add the skill to the user's skills
        Set<SkillEntity> userSkills = userEntity.getUserSkills();
        if (userSkills == null) {
            userSkills = new HashSet<>();
        }
        if (!userSkills.contains(skillEntity)) {
            userSkills.add(skillEntity);
            userEntity.setUserSkills(userSkills);
        }else {
            throw new DuplicatedAttributeException("User already has the specified skill");
        }
        // Add the user to the skill's users
        Set<UserEntity> skillUsers = skillEntity.getUsers();
        if (skillUsers == null) {
            skillUsers = new HashSet<>();
        }
        if (!skillUsers.contains(userEntity)) {
            skillUsers.add(userEntity);
            skillEntity.setUsers(skillUsers);
        }else {
            throw new DuplicatedAttributeException("Skill already has the specified user");
        }
    }

    private void checkSkillExist(String name, SkillTypeEnum type) {
        if (!skillDao.checkSkillExist(name)) {
            SkillEntity skill = new SkillEntity(name, type);
            skillDao.persist(skill);
        }
    }

    @Transactional
    public void addSkillProject(String skillName, SkillTypeEnum type, long projectId) throws DuplicatedAttributeException {
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillName, type);
        // Find the skill by name
        SkillEntity skillEntity = skillDao.findSkillByName(skillName);
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(projectId);
        // Add the skill to the project's skills
        Set<SkillEntity> projectSkills = projectEntity.getProjectSkills();
        if (projectSkills == null) {
            projectSkills = new HashSet<>();
        }
        if (!projectSkills.contains(skillEntity)) {
            projectSkills.add(skillEntity);
            projectEntity.setProjectSkills(projectSkills);
        }else {
            throw new DuplicatedAttributeException("Project already has the specified skill");
        }
        // Add the project to the skill's projects
        Set<ProjectEntity> skillProjects = skillEntity.getProjects();
        if (skillProjects == null) {
            skillProjects = new HashSet<>();
        }
        if (!skillProjects.contains(projectEntity)) {
            skillProjects.add(projectEntity);
            skillEntity.setProjects(skillProjects);
        }else {
            throw new DuplicatedAttributeException("Skill already has the specified project");
        }
    }

    public List<SkillGetDto> getSkills() {
        return convertSkillEntityListToSkillDtoList(skillDao.getAllSkills());
    }

    public List<SkillGetDto> getSkillsByUser(String username) throws EntityNotFoundException {
        List<SkillEntity> skillEntities = skillDao.getSkillsByUsername(username);
        if (skillEntities == null) {
            throw new EntityNotFoundException("No skills associated with this user");
        }
        return convertSkillEntityListToSkillDtoList(skillEntities);
    }

    public List<SkillGetDto> getSkillsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            return new ArrayList<>();
        }
        String lowerCaseFirstLetter = firstLetter.substring(0, 1).toLowerCase();
        List<SkillEntity> skillEntities = skillDao.getSkillsByFirstLetter(lowerCaseFirstLetter);
        return convertSkillEntityListToSkillDtoList(skillEntities);
    }

    public List<SkillGetDto> getSkillsByProject(long projectId) {

        List<SkillEntity> skillEntities = skillDao.getSkillsByProjectId(projectId);
        return convertSkillEntityListToSkillDtoList(skillEntities);
    }

    public List<SkillTypeEnum> getEnumListSkillTypes() {
        List<SkillTypeEnum> skillTypeEnums = new ArrayList<>();
        for (SkillTypeEnum skillTypeEnum : SkillTypeEnum.values()) {
            skillTypeEnums.add(skillTypeEnum);
        }
        return skillTypeEnums;
    }

    @Transactional
    public void removeSkillUser(SkillRemoveUserDto skillRemoveUserDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        // Find the skill by Id
        SkillEntity skillEntity = skillDao.findSkillById(skillRemoveUserDto.getId());
        if (skillEntity == null) {
            throw new EntityNotFoundException("Skill not found");
        }
        // Remove the skill from the user's skills
        Set<SkillEntity> userSkills = userEntity.getUserSkills();
        if (userSkills.contains(skillEntity)) {
            userSkills.remove(skillEntity);
            userEntity.setUserSkills(userSkills);

            // Remove the user from the skill's users
            Set<UserEntity> skillUsers = skillEntity.getUsers();
            skillUsers.remove(userEntity);
            skillEntity.setUsers(skillUsers);
        } else {
            throw new IllegalStateException("User does not have the specified skill");
        }
    }

    @Transactional
    public void removeSkillProject(SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(skillRemoveProjectDto.getProjectId());
        if (projectEntity == null) {
            throw new EntityNotFoundException("Project not found");
        }
        // Find the skill by Id
        SkillEntity skillEntity = skillDao.findSkillById(skillRemoveProjectDto.getId());
        if (skillEntity == null) {
            throw new EntityNotFoundException("Skill not found");
        }
        // Remove the skill from the project's skills
        Set<SkillEntity> projectSkills = projectEntity.getProjectSkills();
        if (projectSkills.contains(skillEntity)) {
            projectSkills.remove(skillEntity);
            projectEntity.setProjectSkills(projectSkills);
        } else {
            throw new IllegalStateException("Skill does not have the specified project");
        }

            // Remove the project from the skill's projects
            Set<ProjectEntity> skillProjects = skillEntity.getProjects();
        if(skillProjects.contains(projectEntity)){
            skillProjects.remove(projectEntity);
            skillEntity.setProjects(skillProjects);
        } else {
            throw new IllegalStateException("Project does not have the specified skill");
        }
    }

    public SkillGetDto convertSkillEntitytoSkillDto(SkillEntity skillEntity) {
        SkillGetDto skillGetDto = new SkillGetDto();
        skillGetDto.setId(skillEntity.getId());
        skillGetDto.setName(skillEntity.getName());
        skillGetDto.setType(skillEntity.getType());
        return skillGetDto;
    }

    public List<SkillGetDto> convertSkillEntityListToSkillDtoList(List<SkillEntity> skillEntities) {
        List<SkillGetDto> skillGetDtos = new ArrayList<>();
        for (SkillEntity i : skillEntities) {
            SkillGetDto skillGetDto = convertSkillEntitytoSkillDto(i);
            skillGetDtos.add(skillGetDto);
        }
        return skillGetDtos;
    }
}
