package aor.fpbackend.service;

import aor.fpbackend.bean.KeywordBean;
import aor.fpbackend.dto.Keyword.KeywordAddDto;
import aor.fpbackend.dto.Keyword.KeywordGetDto;
import aor.fpbackend.dto.Keyword.KeywordRemoveDto;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.ElementAssociationException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.validation.Valid;
import aor.fpbackend.enums.MethodEnum;
/**
 * KeywordService is a JAX-RS resource class that provides RESTful endpoints for managing keywords,
 * including adding, retrieving, and removing keywords.
 */
@Path("/keywords")
public class KeywordService {

    @EJB
    KeywordBean keywordBean;

    /**
     * Adds a keyword to a project.
     *
     * @param keywordAddDto the DTO containing the keyword name and project ID.
     * @throws EntityNotFoundException if the project is not found.
     * @throws ElementAssociationException if there is an issue associating the keyword with the project.
     * @throws DuplicatedAttributeException if the keyword already exists.
     */
    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ADD_KEYWORD)
    public void addKeyword(@Valid KeywordAddDto keywordAddDto) throws EntityNotFoundException, ElementAssociationException, DuplicatedAttributeException {
        keywordBean.addKeyword(keywordAddDto.getName(), keywordAddDto.getProjectId());
    }

    /**
     * Retrieves a list of all keywords.
     *
     * @return a list of KeywordGetDto representing all keywords.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ALL_KEYWORDS)
    public List<KeywordGetDto> getAllKeywords() {
        return keywordBean.getKeywords();
    }

    /**
     * Retrieves a list of keywords for a specific project.
     *
     * @param projectId the ID of the project.
     * @return a list of KeywordGetDto representing the keywords of the specified project.
     */
    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.KEYWORDS_BY_PROJECT)
    public List<KeywordGetDto> getAllKeywordsByProject(@PathParam("projectId") long projectId) {
        return keywordBean.getKeywordsByProject(projectId);
    }

    /**
     * Retrieves a list of keywords that start with the specified first letter.
     *
     * @param firstLetter the first letter to filter keywords by.
     * @return a list of KeywordGetDto representing the keywords that start with the specified letter.
     */
    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.KEYWORDS_FIRST_LETTER)
    public List<KeywordGetDto> getAllKeywordsByFirstLetter(@QueryParam("value") String firstLetter) {
        return keywordBean.getKeywordsByFirstLetter(firstLetter);
    }

    /**
     * Removes a keyword from a project.
     *
     * @param keywordRemoveDto the DTO containing the keyword name and project ID.
     * @throws EntityNotFoundException if the keyword or project is not found.
     * @throws InputValidationException if there is an issue with the input data.
     */
    @PUT
    @Path("/remove/project/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void removeKeyword(@Valid KeywordRemoveDto keywordRemoveDto) throws EntityNotFoundException, InputValidationException {
        keywordBean.removeKeyword(keywordRemoveDto);
    }

}
