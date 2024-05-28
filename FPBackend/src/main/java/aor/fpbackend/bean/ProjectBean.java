package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.ProjectCreateDto;
import aor.fpbackend.dto.ProjectGetDto;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.ProjectEntity;
import aor.fpbackend.enums.ProjectStateEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;


@Stateless
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    ProjectDao projectDao;

    @EJB
    LaboratoryDao labDao;


    public boolean createProject(ProjectCreateDto projectCreateDto) {
        if (projectCreateDto.getLaboratoryId() <= 0) {
            throw new IllegalArgumentException("Laboratory ID must be a positive number");
        }
        if (projectCreateDto.getMotivation() != null && projectCreateDto.getMotivation().isEmpty()) {
            throw new IllegalArgumentException("Motivation, if provided, cannot be empty");
        }
        if (projectCreateDto.getConclusionDate() != null && projectCreateDto.getConclusionDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Conclusion date cannot be in the past");
        }
        ProjectEntity projectEntity = convertProjectDtotoProjectEntity(projectCreateDto);
        projectEntity.setState(ProjectStateEnum.PLANNING);
        projectEntity.setCreationDate(Instant.now());
        projectDao.persist(projectEntity);
        return true;
    }


    public ArrayList<ProjectGetDto> getAllProjects() {
        ArrayList<ProjectEntity> projects = projectDao.findAllProjects();
        if (projects != null && !projects.isEmpty()) {
            return convertProjectEntityListToProjectDtoList(projects);
        } else {
            return new ArrayList<>();
        }
    }

    public ProjectGetDto getProjectDetailsById(long projectId) throws EntityNotFoundException {
        try {
            ProjectEntity projectEntity = projectDao.findProjectById(projectId);
            if (projectEntity != null) {
                return convertProjectEntityToProjectDto(projectEntity);
            } else {
                throw new EntityNotFoundException("Project not found");
            }
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Project not found");
        }
    }



    private ProjectEntity convertProjectDtotoProjectEntity(ProjectCreateDto projectCreateDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectCreateDto.getName());
        projectEntity.setDescription(projectCreateDto.getDescription());
        projectEntity.setMotivation(projectCreateDto.getMotivation());
        projectEntity.setConclusionDate(projectCreateDto.getConclusionDate());
        LaboratoryEntity laboratoryEntity = labDao.findLaboratoryById(projectCreateDto.getLaboratoryId());
        projectEntity.setLaboratory(laboratoryEntity);
        return projectEntity;
    }

    private ProjectGetDto convertProjectEntityToProjectDto(ProjectEntity projectEntity) {
        ProjectGetDto projectGetDto = new ProjectGetDto();
        projectGetDto.setId(projectEntity.getId());
        projectGetDto.setName(projectEntity.getName());
        projectGetDto.setDescription(projectEntity.getDescription());
        projectGetDto.setMotivation(projectEntity.getMotivation());
        projectGetDto.setState(projectEntity.getState());
        projectGetDto.setCreationDate(projectEntity.getCreationDate());
        projectGetDto.setInitialDate(projectEntity.getInitialDate());
        projectGetDto.setFinalDate(projectEntity.getFinalDate());
        projectGetDto.setConclusionDate(projectEntity.getConclusionDate());
        projectGetDto.setLaboratoryId(projectEntity.getLaboratory().getId());
        return projectGetDto;
    }

    private ArrayList<ProjectGetDto> convertProjectEntityListToProjectDtoList(ArrayList<ProjectEntity> projectEntities) {
        ArrayList<ProjectGetDto> projectGetDtos = new ArrayList<>();
        for (ProjectEntity p : projectEntities) {
            ProjectGetDto projectGetDto = convertProjectEntityToProjectDto(p);
            projectGetDtos.add(projectGetDto);
        }
        return projectGetDtos;
    }
}
