package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;


@Path("/memberships")
public class MembershipService {

    @EJB
    MembershipBean memberBean;

    @PUT
    @Path("/ask/join/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASK_TO_JOIN)
    public void askToJoinProject(@PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException {
        memberBean.askToJoinProject(projectId, securityContext);
    }

    @PUT
    @Path("/confirm/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void confirmProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve, @QueryParam("approver") String approverUsername) throws EntityNotFoundException {
        memberBean.confirmProjectInvite(token, approve, approverUsername);
    }

}
