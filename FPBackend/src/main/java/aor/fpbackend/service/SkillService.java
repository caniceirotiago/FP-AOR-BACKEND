package aor.fpbackend.service;

import aor.fpbackend.bean.SkillBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.AttributeAlreadyExistsException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/skills")
public class SkillService {

    @EJB
    SkillBean skillBean;

    // Add Skill to user's skills
    @POST
    @Path("/add/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_SKILL_USER)
    public void addSkill(@Valid SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) throws AttributeAlreadyExistsException {
        skillBean.addSkillUser(skillAddUserDto, securityContext);
    }

    // Add Skill to project's skills
    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_SKILL_PROJECT)
    public void addSkill(@Valid SkillAddProjectDto skillAddProjectDto) throws AttributeAlreadyExistsException {
        skillBean.addSkillProject(skillAddProjectDto.getName(), skillAddProjectDto.getProjectId());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_SKILLS)
    public List<SkillGetDto> getAllSkills() {
          return skillBean.getSkills();
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_BY_USER)
    public List<SkillGetDto> getAllSkillsByUser(@PathParam("username") String username) throws EntityNotFoundException {
        return skillBean.getSkillsByUser(username);
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_BY_PROJECT)
    public List<SkillGetDto> getAllSkillsByProject(@PathParam("projectId") long projectId) {
        return skillBean.getSkillsByProject(projectId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_FIRST_LETTER)
    public List<SkillGetDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_SKILL_USER)
    public void removeSkillFromUser(@Valid SkillRemoveUserDto skillRemoveUserDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        skillBean.removeSkillUser(skillRemoveUserDto, securityContext);
    }

    @PUT
    @Path("/remove/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_SKILL_PROJECT)
    public void removeSkillFromProject(@Valid SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        skillBean.removeSkillProject(skillRemoveProjectDto);
    }

}
