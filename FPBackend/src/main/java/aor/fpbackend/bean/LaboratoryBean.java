package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dto.LaboratoryDto;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.entity.ConfigurationEntity;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class LaboratoryBean implements Serializable {
    private static final long serialVersionUID = 1L;

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


    private LaboratoryEntity convertLaboratoryDtotoLaboratoryEntity(LaboratoryDto laboratoryDto) {
        LaboratoryEntity laboratoryEntity = new LaboratoryEntity();
        laboratoryEntity.setLocation(laboratoryDto.getLocation());
        return laboratoryEntity;
    }

    private LaboratoryDto convertLaboratoryEntitytoLaboratoryDto(LaboratoryEntity laboratoryEntity) {
        LaboratoryDto laboratoryDto = new LaboratoryDto();
        laboratoryDto.setId(laboratoryEntity.getId());
        laboratoryDto.setLocation(laboratoryEntity.getLocation());
        return laboratoryDto;
    }

    private ArrayList<LaboratoryDto> convertLaboratoryEntityListToLaboratoryDtoList(ArrayList<LaboratoryEntity> laboratoryEntities) {
        ArrayList<LaboratoryDto> labDtos = new ArrayList<>();
        for (LaboratoryEntity l : laboratoryEntities) {
            LaboratoryDto labDto = convertLaboratoryEntitytoLaboratoryDto(l);
            labDtos.add(labDto);
        }
        return labDtos;
    }

}
