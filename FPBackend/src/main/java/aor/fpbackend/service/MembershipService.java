package aor.fpbackend.service;

import aor.fpbackend.bean.MembershipBean;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.UserBasicInfoDto;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.filters.RequiresMethodPermission;
import aor.fpbackend.filters.RequiresProjectRolePermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;


@Path("/memberships")
public class MembershipService {

    @EJB
    MembershipBean memberBean;
    @EJB
    UserDao userDao;

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

    @POST
    @Path("/add/{username}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void addUserToProject(@PathParam("username") String username, @PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException, UserNotFoundException, InputValidationException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity authUserEntity = userDao.findUserById(authUserDto.getUserId());
        memberBean.addUserToProject(username, projectId, false, false, authUserEntity.getUsername());
    }

    @PUT
    @Path("/accept/project")
    @Consumes(MediaType.APPLICATION_JSON)
    public void acceptProjectInvite(@QueryParam("token") String token, @QueryParam("approve") boolean approve) throws EntityNotFoundException {
        memberBean.acceptProjectInvite(token, approve);
    }

    @PUT
    @Path("/remove/{username}/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectRolePermission(ProjectRoleEnum.PROJECT_MANAGER)
    public void removeUserFromProject(@PathParam("username") String username, @PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws EntityNotFoundException {
        memberBean.removeUserFromProject(username, projectId, securityContext);
    }
    @GET
    @Path("/projectids-byuserid/securitycontext")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getProjectIdsByUserId(@Context SecurityContext securityContext) {
        return memberBean.getProjectIdsByUserId(securityContext);
    }
    @GET
    @Path("/first/letter/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserBasicInfoDto> getBasicInfoByFirstLetter(@QueryParam("value") String firstLetter, @PathParam("projectId") long projectId) {
        System.out.println("firstLetter: " + firstLetter + " projectId: " + projectId);
        return memberBean.getUsersBasicInfoByFirstLetter(firstLetter, projectId);
    }





}
