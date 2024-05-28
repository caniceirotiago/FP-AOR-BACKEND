package aor.fpbackend.service;

import aor.fpbackend.bean.AssetBean;
import aor.fpbackend.dto.AssetAddDto;
import aor.fpbackend.dto.ProjectCreateDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/assets")
public class AssetService {
    @EJB
    AssetBean assetBean;


    @POST
    @Path("/add/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_PROJECT)
    public void createAsset(@Valid AssetAddDto assetAddDto) {
        assetBean.addAsset(assetAddDto);
    }
//
//    @GET
//    @Path("")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.ALL_PROJECTS)
//    public ArrayList<ProjectGetDto> getAllAssets() {
//        return assetBean.getAllAssets();
//    }
//
//    @GET
//    @Path("/first/letter")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RequiresPermission(MethodEnum.SKILL_FIRST_LETTER)
//    public List<SkillGetDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) {
//        return assetBean.getSkillsByFirstLetter(firstLetter);
//    }


}