package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.ProjectGetDto;
import aor.fpbackend.entity.ProjectEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.util.ArrayList;


@Stateless
    public class ProjectBean implements Serializable {
        private static final long serialVersionUID = 1L;

        @EJB
        ProjectDao projectDao;


    public boolean createProject(ProjectGetDto projectGetDto) {
        ProjectEntity projectEntity = convertProjectDtotoProjectEntity(projectGetDto);
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


    private ProjectEntity convertProjectDtotoProjectEntity(ProjectGetDto projectGetDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectGetDto.getName());
        projectEntity.setDescription(projectGetDto.getDescription());
        projectEntity.setMotivation(projectGetDto.getMotivation());
        projectEntity.setState(projectGetDto.getState());
        projectEntity.setCreationDate(projectGetDto.getCreationDate());
        projectEntity.setInitialDate(projectGetDto.getInitialDate());
        projectEntity.setFinalDate(projectGetDto.getFinalDate());
        projectEntity.setConclusionDate(projectGetDto.getConclusionDate());
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
