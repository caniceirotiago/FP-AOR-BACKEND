package aor.fpbackend.service;

import aor.fpbackend.bean.ProjectBean;
import aor.fpbackend.dto.ProjectCreateDto;
import aor.fpbackend.dto.ProjectGetDto;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;


@Path("/projects")
public class ProjectService {
    @EJB
    ProjectBean projectBean;


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createProject(@Valid ProjectCreateDto projectCreateDto) {
        projectBean.createProject(projectCreateDto);
    }


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ProjectGetDto> getAllProjects() {
        return projectBean.getAllProjects();
    }


}
