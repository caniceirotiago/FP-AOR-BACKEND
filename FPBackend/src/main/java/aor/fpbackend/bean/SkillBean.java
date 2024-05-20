package aor.fpbackend.bean;

import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dto.SkillDto;
import aor.fpbackend.entity.SkillEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class SkillBean implements Serializable {
    @EJB
    SkillDao skillDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SkillBean.class);

    public void createSkill(String name) {
        SkillEntity skill = new SkillEntity(name);
        skillDao.persist(skill);
    }
    public List<SkillDto> getSkills() {
        return convertSkillEntityListToSkillDtoList(skillDao.getAllSkills());
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
