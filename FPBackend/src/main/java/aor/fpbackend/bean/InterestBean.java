package aor.fpbackend.bean;

import aor.fpbackend.dao.InterestDao;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.entity.InterestEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class InterestBean implements Serializable {
    @EJB
    InterestDao interestDao;
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(InterestBean.class);

    public void createInterest(String name) {
        InterestEntity interest = new InterestEntity(name);
        interestDao.persist(interest);
    }
    public List<InterestDto> getInterests() {
        return convertInterestEntityListToInterestDtoList(interestDao.getAllInterests());
    }
    public List<InterestDto> getInterestsByFirstLetter(String firstLetter) {
        if (firstLetter.length() != 1 || !Character.isLetter(firstLetter.charAt(0))) {
            LOGGER.error("Invalid first letter: " + firstLetter);
            return new ArrayList<>();
        }
        return convertInterestEntityListToInterestDtoList(interestDao.getInterestsByFirstLetter(firstLetter.charAt(0)));
    }

    public InterestDto convertInterestEntitytoInterestDto(InterestEntity interestEntity) {
        InterestDto interestDto = new InterestDto();
        interestDto.setId(interestEntity.getId());
        interestDto.setName(interestEntity.getName());
        return interestDto;
    }
    public List<InterestDto> convertInterestEntityListToInterestDtoList(List<InterestEntity> interestEntities) {
        List<InterestDto> interestDtos = new ArrayList<>();
        for (InterestEntity i : interestEntities) {
            InterestDto interestDto = convertInterestEntitytoInterestDto(i);
            interestDtos.add(interestDto);
        }
        return interestDtos;
    }
}
