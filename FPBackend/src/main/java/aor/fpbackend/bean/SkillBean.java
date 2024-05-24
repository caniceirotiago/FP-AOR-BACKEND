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
        // Find the skill by name, if it exists
        SkillEntity skillEntity = skillDao.findSkillByName(skillDto.getName());
        // If the skill doesn't exist, create and persist it
        if(skillEntity==null) {
            SkillEntity skill = new SkillEntity(skillDto.getName());
            skillDao.persist(skill);
        }
        // Get the authenticated user
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        // Add the skill to the user's skills
        Set<SkillEntity> userSkills = userEntity.getUserSkills();
        if (userSkills == null) {
            userSkills = new HashSet<>();
        }
        userSkills.add(skillEntity);
        userEntity.setUserSkills(userSkills);
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
