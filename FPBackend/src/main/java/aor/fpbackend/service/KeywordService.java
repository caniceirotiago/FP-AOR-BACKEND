package aor.fpbackend.service;

import aor.fpbackend.bean.KeywordBean;
import aor.fpbackend.dto.KeywordDto;
import aor.fpbackend.dto.ProjectDto;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

import aor.fpbackend.filters.RequiresPermission;
import jakarta.validation.Valid;
import aor.fpbackend.enums.MethodEnum;

@Path("/keywords")
public class KeywordService {

    @EJB
    KeywordBean keywordBean;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_KEYWORD)
    public void addKeyword(@Valid KeywordDto keywordDto, ProjectDto projectDto) {
        keywordBean.addKeyword(keywordDto, projectDto);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_KEYWORDS)
    public List<KeywordDto> getAllKeywords() {
        return keywordBean.getKeywords();
    }

    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_BY_PROJECT)
    public List<KeywordDto> getAllKeywordsByProject(ProjectDto projectDto) {
        return keywordBean.getKeywordsByProject(projectDto);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_FIRST_LETTER)
    public List<KeywordDto> getAllKeywordsByFirstLetter(@QueryParam("value") String firstLetter) {
        return keywordBean.getKeywordsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_KEYWORD)
    public void removeKeyword(@Valid KeywordDto keywordDto, ProjectDto projectDto) throws EntityNotFoundException {
        keywordBean.removeKeyword(keywordDto, projectDto);
    }

}
