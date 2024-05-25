package aor.fpbackend.bean;

import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.dto.SkillDto;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
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
    UserDao userDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SkillBean.class);

    public void addSkill(SkillDto skillDto, @Context SecurityContext securityContext) {
        // Ensure the skill exists, creating it if necessary
        checkSkillExist(skillDto.getName());
        // Find the skill by name
        SkillEntity skillEntity = skillDao.findSkillByName(skillDto.getName());
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
        if(!skillDao.checkSkillExist(name)) {
            SkillEntity skill = new SkillEntity(name);
            skillDao.persist(skill);
        }
    }


    public List<SkillDto> getSkills() {
        return convertSkillEntityListToSkillDtoList(skillDao.getAllSkills());
    }

    public List<SkillDto> getSkillsByUser(@Context SecurityContext securityContext) {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        return convertSkillEntityListToSkillDtoList(skillDao.getSkillsByUserId(authUserDto.getUserId()));
    }

    public List<SkillDto> getSkillsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        return convertSkillEntityListToSkillDtoList(skillDao.getSkillsByFirstLetter(firstLetter.charAt(0)));
    }

    public SkillDto convertSkillEntitytoSkillDto(SkillEntity SkillEntity) {
        SkillDto skillDto = new SkillDto();
        skillDto.setId(SkillEntity.getId());
        skillDto.setName(SkillEntity.getName());
        return skillDto;
    }
    public List<SkillDto> convertSkillEntityListToSkillDtoList(List<SkillEntity> SkillEntities) {
        List<SkillDto> SkillDtos = new ArrayList<>();
        for (SkillEntity i : SkillEntities) {
            SkillDto skillDto = convertSkillEntitytoSkillDto(i);
            SkillDtos.add(skillDto);
        }
        return SkillDtos;
    }
}
