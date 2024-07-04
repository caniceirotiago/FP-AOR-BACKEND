package aor.fpbackend.service;

import aor.fpbackend.bean.SkillBean;
import aor.fpbackend.dto.Skill.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.SkillTypeEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
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
    @RequiresMethodPermission(MethodEnum.ADD_SKILL_USER)
    public void addSkill(@Valid SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException, EntityNotFoundException, UserNotFoundException, DatabaseOperationException {
        skillBean.addSkillUser(skillAddUserDto, securityContext);
    }

    // Add Skill to project's skills
    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_SKILL_PROJECT)
    public void addSkill(@Valid SkillAddProjectDto skillAddProjectDto) throws DuplicatedAttributeException, DatabaseOperationException, ElementAssociationException, EntityNotFoundException {
        skillBean.addSkillProject(skillAddProjectDto.getName(), skillAddProjectDto.getType(), skillAddProjectDto.getProjectId());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_SKILLS)
    public List<SkillGetDto> getAllSkills() throws DatabaseOperationException {
        return skillBean.getSkills();
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_BY_USER)
    public List<SkillGetDto> getAllSkillsByUser(@PathParam("username") String username) throws EntityNotFoundException, DatabaseOperationException {
        return skillBean.getSkillsByUser(username);
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_BY_PROJECT)
    public List<SkillGetDto> getAllSkillsByProject(@PathParam("projectId") long projectId) throws DatabaseOperationException {
        return skillBean.getSkillsByProject(projectId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_FIRST_LETTER)
    public List<SkillGetDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) throws DatabaseOperationException {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILL_ENUMS)
    public List<SkillTypeEnum> getSkillTypes() {
        return skillBean.getEnumListSkillTypes();
    }

    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_SKILL_USER)
    public void removeSkillFromUser(@Valid SkillRemoveUserDto skillRemoveUserDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        skillBean.removeSkillUser(skillRemoveUserDto, securityContext);
    }

    // /{projectId} just for filter validation
    @PUT
    @Path("/remove/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeSkillFromProject(@Valid SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        skillBean.removeSkillProject(skillRemoveProjectDto);
    }

}
