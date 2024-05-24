package aor.fpbackend.service;

import aor.fpbackend.bean.InterestBean;
import aor.fpbackend.bean.SkillBean;
import aor.fpbackend.dto.InterestDto;
import aor.fpbackend.dto.SkillDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/skills")
public class SkillService {

    @EJB
    SkillBean skillBean;
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createSkill(SkillDto skillDto) {
        skillBean.createSkill(skillDto.getName());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SkillDto> getAllSkills() {
          return skillBean.getSkills();
    }

    @GET
    @Path("/first/letter")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SkillDto> getAllSkillsByFirstLetter(@QueryParam("value") String firstLetter) {
        return skillBean.getSkillsByFirstLetter(firstLetter);
    }

}
