package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.ProjectEntity;
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
    public void addSkillUser(SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) {
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillAddUserDto.getName());
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
        }
        // Add the user to the skill's users
        Set<UserEntity> skillUsers = skillEntity.getUsers();
        if (skillUsers == null) {
            skillUsers = new HashSet<>();
        }
        if (!skillUsers.contains(userEntity)) {
            skillUsers.add(userEntity);
            skillEntity.setUsers(skillUsers);
        }
    }

    private void checkSkillExist(String name) {
        if (!skillDao.checkSkillExist(name)) {
            SkillEntity skill = new SkillEntity(name);
            skillDao.persist(skill);
        }
    }

    @Transactional
    public void addSkillProject(SkillAddProjectDto skillAddProjectDto) {
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillAddProjectDto.getName());
        // Find the skill by name
        SkillEntity skillEntity = skillDao.findSkillByName(skillAddProjectDto.getName());
        // Find the project by id
        ProjectEntity projectEntity = projectDao.findProjectById(skillAddProjectDto.getProjectId());
        // Add the skill to the project's skills
        Set<SkillEntity> projectSkills = projectEntity.getProjectSkills();
        if (projectSkills == null) {
            projectSkills = new HashSet<>();
        }
        if (!projectSkills.contains(skillEntity)) {
            projectSkills.add(skillEntity);
            projectEntity.setProjectSkills(projectSkills);
        }
        // Add the project to the skill's projects
        Set<ProjectEntity> skillProjects = skillEntity.getProjects();
        if (skillProjects == null) {
            skillProjects = new HashSet<>();
        }
        if (!skillProjects.contains(projectEntity)) {
            skillProjects.add(projectEntity);
            skillEntity.setProjects(skillProjects);
        }
    }

    public List<SkillGetDto> getSkills() {
        return convertSkillEntityListToSkillDtoList(skillDao.getAllSkills());
    }

    public List<SkillGetDto> getSkillsByUser(@Context SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        return convertSkillEntityListToSkillDtoList(skillDao.getSkillsByUserId(authUserDto.getUserId()));
    }

    public List<SkillGetDto> getSkillsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        List<SkillEntity> skillEntities = skillDao.getSkillsByFirstLetter(firstLetter.charAt(0));
        return convertSkillEntityListToSkillDtoList(skillEntities);
    }

    @Transactional
    public void removeSkill(SkillRemoveDto skillRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        if (userEntity == null) {
            throw new UserNotFoundException("User not found");
        }
        // Find the skill by Id
        SkillEntity skillEntity = skillDao.findSkillById(skillRemoveDto.getId());
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

    public SkillGetDto convertSkillEntitytoSkillDto(SkillEntity SkillEntity) {
        SkillGetDto skillGetDto = new SkillGetDto();
        skillGetDto.setId(SkillEntity.getId());
        skillGetDto.setName(SkillEntity.getName());
        return skillGetDto;
    }

    public List<SkillGetDto> convertSkillEntityListToSkillDtoList(List<SkillEntity> SkillEntities) {
        List<SkillGetDto> skillGetDtos = new ArrayList<>();
        for (SkillEntity i : SkillEntities) {
            SkillGetDto skillGetDto = convertSkillEntitytoSkillDto(i);
            skillGetDtos.add(skillGetDto);
        }
        return skillGetDtos;
    }
}
