package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;


@Path("/memberships")
public class MembershipService {

    @EJB
    MembershipBean memberBean;

    @PUT
    @Path("/ask/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASK_TO_JOIN)
    public void askToJoinProject(@Valid ProjectAskJoinDto projectAskJoinDto, @Context SecurityContext securityContext) throws EntityNotFoundException {
        memberBean.askToJoinProject(projectAskJoinDto, securityContext);
    }

    @PUT
    @Path("/confirm/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void confirmProjectInvite(@QueryParam("token") String token, boolean approve, @Context SecurityContext securityContext) throws EntityNotFoundException {
        memberBean.confirmProjectInvite(token, approve, securityContext);
    }

}
