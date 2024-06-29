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

/**
 * LaboratoryBean is a stateless session bean responsible for managing laboratory entities.
 * <p>
 * This bean handles the creation of laboratories if they do not already exist, and the retrieval
 * of laboratory information in the form of DTOs (Data Transfer Objects).
 * </p>
 * <p>
 * Key functionalities provided by this bean include:
 * <ul>
 *     <li>Creating laboratories if they do not exist.</li>
 *     <li>Retrieving all laboratories and converting them to DTOs.</li>
 * </ul>
 * </p>
 * <p>
 * The class uses dependency injection to obtain instances of LaboratoryDao, promoting a clean architecture.
 */
@Stateless
public class LaboratoryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LaboratoryBean.class);

    @EJB
    LaboratoryDao laboratoryDao;


    /**
     * Creates a laboratory if it does not already exist.
     *
     * @param location the location of the laboratory
     * @throws DatabaseOperationException if an error occurs while accessing the database
     */
    public void createLaboratoryIfNotExists(LocationEnum location) throws DatabaseOperationException {
        if (!laboratoryDao.checkLaboratoryExist(location)) {
            LaboratoryEntity laboratory = new LaboratoryEntity(location);
            laboratoryDao.persist(laboratory);
        }
    }

    /**
     * Retrieves all laboratories and converts them to a list of LaboratoryDto.
     *
     * @return a list of LaboratoryDto objects
     */

    public ArrayList<LaboratoryDto> getLaboratories() {
        ArrayList<LaboratoryEntity> labs = laboratoryDao.findAllLaboratories();
        if (labs != null && !labs.isEmpty()) {
            return convertLaboratoryEntityListToLaboratoryDtoList(labs);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Converts a LaboratoryEntity to a LaboratoryDto.
     *
     * @param laboratoryEntity the LaboratoryEntity to convert
     * @return the corresponding LaboratoryDto
     */
    public LaboratoryDto convertLaboratoryEntityToLaboratoryDto(LaboratoryEntity laboratoryEntity) {
        LaboratoryDto laboratoryDto = new LaboratoryDto();
        laboratoryDto.setId(laboratoryEntity.getId());
        laboratoryDto.setLocation(laboratoryEntity.getLocation());
        return laboratoryDto;
    }

    /**
     * Converts a list of LaboratoryEntity objects to a list of LaboratoryDto objects.
     *
     * @param laboratoryEntities the list of LaboratoryEntity objects to convert
     * @return a list of LaboratoryDto objects
     */
    public ArrayList<LaboratoryDto> convertLaboratoryEntityListToLaboratoryDtoList(ArrayList<LaboratoryEntity> laboratoryEntities) {
        ArrayList<LaboratoryDto> labDtos = new ArrayList<>();
        for (LaboratoryEntity l : laboratoryEntities) {
            LaboratoryDto labDto = convertLaboratoryEntityToLaboratoryDto(l);
            labDtos.add(labDto);
        }
        return labDtos;
    }

}
