package aor.fpbackend.service;

import aor.fpbackend.bean.SkillBean;
import aor.fpbackend.dto.SkillAddProjectDto;
import aor.fpbackend.dto.SkillAddUserDto;
import aor.fpbackend.dto.SkillGetDto;
import aor.fpbackend.dto.SkillRemoveDto;
import aor.fpbackend.enums.MethodEnum;
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
    @Path("/add/to/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_SKILL_USER)
    public void addSkill(@Valid SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) {
        skillBean.addSkillUser(skillAddUserDto, securityContext);
    }

    // Add Skill to project's skills
    @POST
    @Path("/add/to/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_SKILL_PROJECT)
    public void addSkill(@Valid SkillAddProjectDto skillAddProjectDto) {
        skillBean.addSkillProject(skillAddProjectDto);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_SKILLS)
    public List<SkillGetDto> getAllSkills() {
          return skillBean.getSkills();
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_BY_USER)
    public List<SkillGetDto> getAllSkillsByUser(@Context SecurityContext securityContext) {
        return skillBean.getSkillsByUser(securityContext);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_FIRST_LETTER)
    public List<SkillGetDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_SKILL)
    public void removeSkill(@Valid SkillRemoveDto skillRemoveDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        skillBean.removeSkill(skillRemoveDto, securityContext);
    }

}
