package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.bean.SkillBean;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.dto.SkillDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/skills")
public class SkillService {

    @EJB
    SkillBean skillBean;
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ADD_SKILL)
    public void addSkill(SkillDto skillDto, @Context SecurityContext securityContext) {
        skillBean.addSkill(skillDto, securityContext);
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.ALL_SKILLS)
    public List<SkillDto> getAllSkills() {
          return skillBean.getSkills();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_BY_USER)
    public List<SkillDto> getAllSkillsByUser(@Context SecurityContext securityContext) {
        return skillBean.getSkillsByUser(securityContext);
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(MethodEnum.SKILL_FIRST_LETTER)
    public List<SkillDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

}
