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
/**
 * SkillService is a JAX-RS resource class that provides RESTful endpoints for skill management,
 * including adding, retrieving, and removing skills for users and projects.
 */
@Path("/skills")
public class SkillService {

    @EJB
    SkillBean skillBean;

    /**
     * Adds a skill to a user's skills.
     *
     * @param skillAddUserDto the skill data transfer object for adding a skill to a user.
     * @param securityContext the security context.
     * @throws DuplicatedAttributeException if the skill already exists for the user.
     * @throws EntityNotFoundException if the user or skill is not found.
     * @throws UserNotFoundException if the user is not found.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @POST
    @Path("/add/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_SKILL_USER)
    public void addSkill(@Valid SkillAddUserDto skillAddUserDto, @Context SecurityContext securityContext) throws DuplicatedAttributeException, EntityNotFoundException, UserNotFoundException, DatabaseOperationException {
        skillBean.addSkillUser(skillAddUserDto, securityContext);
    }

    /**
     * Adds a skill to a project's skills.
     *
     * @param skillAddProjectDto the skill data transfer object for adding a skill to a project.
     * @throws DuplicatedAttributeException if the skill already exists for the project.
     * @throws DatabaseOperationException if a database error occurs.
     * @throws ElementAssociationException if there is an association error.
     * @throws EntityNotFoundException if the project or skill is not found.
     */
    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_SKILL_PROJECT)
    public void addSkill(@Valid SkillAddProjectDto skillAddProjectDto) throws DuplicatedAttributeException, DatabaseOperationException, ElementAssociationException, EntityNotFoundException {
        skillBean.addSkillProject(skillAddProjectDto.getName(), skillAddProjectDto.getType(), skillAddProjectDto.getProjectId());
    }

    /**
     * Retrieves all skills.
     *
     * @return a list of skill data transfer objects.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_SKILLS)
    public List<SkillGetDto> getAllSkills() throws DatabaseOperationException {
        return skillBean.getSkills();
    }

    /**
     * Retrieves all skills for a given user.
     *
     * @param username the username.
     * @return a list of skill data transfer objects.
     * @throws EntityNotFoundException if the user is not found.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_BY_USER)
    public List<SkillGetDto> getAllSkillsByUser(@PathParam("username") String username) throws EntityNotFoundException, DatabaseOperationException {
        return skillBean.getSkillsByUser(username);
    }

    /**
     * Retrieves all skills for a given project.
     *
     * @param projectId the project ID.
     * @return a list of skill data transfer objects.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_BY_PROJECT)
    public List<SkillGetDto> getAllSkillsByProject(@PathParam("projectId") long projectId) throws DatabaseOperationException {
        return skillBean.getSkillsByProject(projectId);
    }

    /**
     * Retrieves all skills that start with the specified first letter.
     *
     * @param firstLetter the first letter to filter skills by.
     * @return a list of skill data transfer objects.
     * @throws DatabaseOperationException if a database error occurs.
     */
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILLS_FIRST_LETTER)
    public List<SkillGetDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) throws DatabaseOperationException {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

    /**
     * Retrieves a list of possible skill types.
     *
     * @return a list of skill type enums.
     */
    @GET
    @Path("/enum/types")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.SKILL_ENUMS)
    public List<SkillTypeEnum> getSkillTypes() {
        return skillBean.getEnumListSkillTypes();
    }

    /**
     * Removes a skill from a user.
     *
     * @param skillRemoveUserDto the skill data transfer object for removing a skill from a user.
     * @param securityContext the security context.
     * @throws UserNotFoundException if the user is not found.
     * @throws EntityNotFoundException if the skill is not found.
     */
    @PUT
    @Path("/remove/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.REMOVE_SKILL_USER)
    public void removeSkillFromUser(@Valid SkillRemoveUserDto skillRemoveUserDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        skillBean.removeSkillUser(skillRemoveUserDto, securityContext);
    }

    /**
     * Removes a skill from a project.
     *
     * @param skillRemoveProjectDto the skill data transfer object for removing a skill from a project.
     * @throws EntityNotFoundException if the skill or project is not found.
     */
    @PUT
    @Path("/remove/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeSkillFromProject(@Valid SkillRemoveProjectDto skillRemoveProjectDto) throws EntityNotFoundException {
        skillBean.removeSkillProject(skillRemoveProjectDto);
    }

}
