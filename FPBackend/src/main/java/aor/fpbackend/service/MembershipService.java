package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UnauthorizedAccessException;
import aor.fpbackend.exception.UserNotFoundException;
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

    @POST
    @Path("/ask/join/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.ASK_TO_JOIN)
    public void askToJoinProject(@PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, DuplicatedAttributeException {
        memberBean.askToJoinProject(projectId, securityContext);
    }

    @PUT
    @Path("/confirm/project")
    @Consumes(MediaType.APPLICATION_JSON)
    public void confirmAskToJoinProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve, @QueryParam("approver") String approverUsername) throws EntityNotFoundException, UserNotFoundException, UnauthorizedAccessException {
        memberBean.confirmAskToJoinProjectInvite(token, approve, approverUsername);
    }

    @PUT
    @Path("/accept/project")
    @Consumes(MediaType.APPLICATION_JSON)
    public void acceptProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve) throws EntityNotFoundException {
        memberBean.acceptProjectInvite(token, approve);
    }

}
