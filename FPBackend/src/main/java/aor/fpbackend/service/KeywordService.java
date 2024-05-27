package aor.fpbackend.service;

import aor.fpbackend.bean.KeywordBean;
import aor.fpbackend.dto.KeywordAddDto;
import aor.fpbackend.dto.KeywordGetDto;
import aor.fpbackend.dto.KeywordRemoveDto;
import aor.fpbackend.dto.ProjectDto;
import aor.fpbackend.exception.EntityNotFoundException;
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
    public void addKeyword(@Valid KeywordAddDto keywordAddDto, ProjectDto projectDto) {
        keywordBean.addKeyword(keywordAddDto, projectDto);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_KEYWORDS)
    public List<KeywordGetDto> getAllKeywords() {
        return keywordBean.getKeywords();
    }

    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_BY_PROJECT)
    public List<KeywordGetDto> getAllKeywordsByProject(ProjectDto projectDto) {
        return keywordBean.getKeywordsByProject(projectDto);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_FIRST_LETTER)
    public List<KeywordGetDto> getAllKeywordsByFirstLetter(@QueryParam("value") String firstLetter) {
        return keywordBean.getKeywordsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_KEYWORD)
    public void removeKeyword(@Valid KeywordRemoveDto keywordRemoveDto, ProjectDto projectDto) throws EntityNotFoundException {
        keywordBean.removeKeyword(keywordRemoveDto, projectDto);
    }

}
