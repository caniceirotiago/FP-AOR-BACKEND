package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.ReportProjectsLocationDto;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Stateless
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(aor.fpbackend.bean.ReportBean.class);
    @EJB
    ProjectDao projectDao;
    @EJB
    LaboratoryDao laboratoryDao;



    public List<ReportProjectsLocationDto> getProjectCountByLocation() {
        Map<LocationEnum, Long> projectCountByLaboratory = countProjectsByLocation();
        long totalProjects = projectCountByLaboratory.values().stream().mapToLong(Long::longValue).sum();

        List<ReportProjectsLocationDto> projectCountDtos = new ArrayList<>();

        for (Map.Entry<LocationEnum, Long> entry : projectCountByLaboratory.entrySet()) {
            double percentage = (double) entry.getValue() * 100 / totalProjects;
            projectCountDtos.add(new ReportProjectsLocationDto(entry.getKey(), entry.getValue(), percentage));
        }
        return projectCountDtos;
    }

    private Map<LocationEnum, Long> countProjectsByLocation() {
        List<Object[]> results = projectDao.countProjectsByLaboratory();

        Map<LocationEnum, Long> projectCountByLaboratory = new HashMap<>();
        for (Object[] result : results) {
            LocationEnum laboratoryLocation = (LocationEnum) result[0];
            Long count = (Long) result[1];
            projectCountByLaboratory.put(laboratoryLocation, count);
        }
        return projectCountByLaboratory;
    }
}
