package aor.fpbackend.service;

import aor.fpbackend.bean.KeywordBean;
import aor.fpbackend.bean.TaskBean;
import aor.fpbackend.dto.KeywordAddDto;
import aor.fpbackend.dto.KeywordGetDto;
import aor.fpbackend.dto.KeywordRemoveDto;
import aor.fpbackend.dto.TaskAddDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;


@Path("/tasks")
public class TaskService {

    @EJB
    TaskBean taskBean;

    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_TASK)
    public void addKeyword(@Valid TaskAddDto taskAddDto) throws EntityNotFoundException, InputValidationException {
        taskBean.addTask(taskAddDto);
    }

//    @GET
//    @Path("")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.ALL_KEYWORDS)
//    public List<KeywordGetDto> getAllKeywords() {
//        return taskBean.getKeywords();
//    }
//
//    @GET
//    @Path("/project/{projectId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.KEYWORD_BY_PROJECT)
//    public List<KeywordGetDto> getAllKeywordsByProject(@PathParam("projectId") long projectId) {
//        return taskBean.getKeywordsByProject(projectId);
//    }

}
