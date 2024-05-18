package aor.fpbackend.bean;

import aor.fpbackend.dao.ProjectDao;
import aor.fpbackend.dto.ProjectDto;
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


        //TODO
        // Criação de projecto sem definir Laboratório, Membros, Skills...
    public boolean createProject(ProjectDto projectDto) {
        ProjectEntity projectEntity = convertProjectDtotoProjectEntity(projectDto);
        projectDao.persist(projectEntity);
        return true;
    }


    public ArrayList<ProjectDto> getAllProjects() {
        ArrayList<ProjectEntity> projects = projectDao.findAllProjects();
        if (projects != null && !projects.isEmpty()) {
            return convertProjectEntityListToProjectDtoList(projects);
        } else {
            return new ArrayList<>();
        }
    }


    private ProjectEntity convertProjectDtotoProjectEntity(ProjectDto projectDto) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectDto.getName());
        projectEntity.setDescription(projectDto.getDescription());
        projectEntity.setMotivation(projectDto.getMotivation());
        projectEntity.setState(projectDto.getState());
        projectEntity.setCreationDate(projectDto.getCreationDate());
        projectEntity.setInitialDate(projectDto.getInitialDate());
        projectEntity.setFinalDate(projectDto.getFinalDate());
        projectEntity.setConclusionDate(projectDto.getConclusionDate());
        projectEntity.setMembersCount(projectDto.getMembersCount());
        return projectEntity;
    }

    private ProjectDto convertProjectEntityToProjectDto(ProjectEntity projectEntity) {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(projectEntity.getId());
        projectDto.setName(projectEntity.getName());
        projectDto.setDescription(projectEntity.getDescription());
        projectDto.setMotivation(projectEntity.getMotivation());
        System.out.println(projectEntity.getState());
        projectDto.setState(projectEntity.getState());
        projectDto.setCreationDate(projectEntity.getCreationDate());
        projectDto.setInitialDate(projectEntity.getInitialDate());
        projectDto.setFinalDate(projectEntity.getFinalDate());
        projectDto.setConclusionDate(projectEntity.getConclusionDate());
        projectDto.setMembersCount(projectEntity.getMembersCount());
        return projectDto;
    }

    private ArrayList<ProjectDto> convertProjectEntityListToProjectDtoList(ArrayList<ProjectEntity> projectEntities) {
        ArrayList<ProjectDto> projectDtos = new ArrayList<>();
        for (ProjectEntity p : projectEntities) {
            ProjectDto projectDto = convertProjectEntityToProjectDto(p);
            projectDtos.add(projectDto);
        }
        return projectDtos;
    }
}
