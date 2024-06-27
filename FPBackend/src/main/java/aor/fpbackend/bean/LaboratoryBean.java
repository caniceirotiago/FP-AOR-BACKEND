package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dto.Laboratory.LaboratoryDto;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.ArrayList;


@Stateless
public class LaboratoryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LaboratoryBean.class);

    @EJB
    LaboratoryDao laboratoryDao;

    public void createLaboratoryIfNotExists(LocationEnum location) throws DatabaseOperationException {
        if (!laboratoryDao.checkLaboratoryExist(location)) {
            LaboratoryEntity laboratory = new LaboratoryEntity(location);
            laboratoryDao.persist(laboratory);
        }
    }

    public ArrayList<LaboratoryDto> getLaboratories() {
        ArrayList<LaboratoryEntity> labs = laboratoryDao.findAllLaboratories();
        if (labs != null && !labs.isEmpty()) {
            return convertLaboratoryEntityListToLaboratoryDtoList(labs);
        } else {
            return new ArrayList<>();
        }
    }

    public LaboratoryDto convertLaboratoryEntityToLaboratoryDto(LaboratoryEntity laboratoryEntity) {
        LaboratoryDto laboratoryDto = new LaboratoryDto();
        laboratoryDto.setId(laboratoryEntity.getId());
        laboratoryDto.setLocation(laboratoryEntity.getLocation());
        return laboratoryDto;
    }

    public ArrayList<LaboratoryDto> convertLaboratoryEntityListToLaboratoryDtoList(ArrayList<LaboratoryEntity> laboratoryEntities) {
        ArrayList<LaboratoryDto> labDtos = new ArrayList<>();
        for (LaboratoryEntity l : laboratoryEntities) {
            LaboratoryDto labDto = convertLaboratoryEntityToLaboratoryDto(l);
            labDtos.add(labDto);
        }
        return labDtos;
    }

}
