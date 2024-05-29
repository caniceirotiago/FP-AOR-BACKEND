package aor.fpbackend.service;

import aor.fpbackend.bean.KeywordBean;
import aor.fpbackend.dto.KeywordAddDto;
import aor.fpbackend.dto.KeywordGetDto;
import aor.fpbackend.dto.KeywordRemoveDto;
import aor.fpbackend.exception.AttributeAlreadyExistsException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
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
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_KEYWORD)
    public void addKeyword(@Valid KeywordAddDto keywordAddDto) throws EntityNotFoundException, InputValidationException {
        if (keywordAddDto != null) {
            keywordBean.addKeyword(keywordAddDto.getName(), keywordAddDto.getType(), keywordAddDto.getProjectId());
        } else {
            throw new InputValidationException("Invalid Dto");
        }
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_KEYWORDS)
    public List<KeywordGetDto> getAllKeywords() {
        return keywordBean.getKeywords();
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_BY_PROJECT)
    public List<KeywordGetDto> getAllKeywordsByProject(@PathParam("projectId") long projectId) {
        return keywordBean.getKeywordsByProject(projectId);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.KEYWORD_FIRST_LETTER)
    public List<KeywordGetDto> getAllKeywordsByFirstLetter(@QueryParam("value") String firstLetter) {
        return keywordBean.getKeywordsByFirstLetter(firstLetter);
    }

    @PUT
    @Path("/remove/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.REMOVE_KEYWORD)
    public void removeKeyword(@Valid KeywordRemoveDto keywordRemoveDto) throws EntityNotFoundException {
        keywordBean.removeKeyword(keywordRemoveDto);
    }

}
