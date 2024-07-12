package aor.fpbackend.service;

import aor.fpbackend.bean.GroupMessageBean;
import aor.fpbackend.dto.GroupMessage.GroupMessageGetDto;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
/**
 * GroupMessageService is a JAX-RS resource class that provides RESTful endpoints for managing group messages
 * within a project.
 */
@Path("/group/messages")
public class GroupMessageService {
    @EJB
    GroupMessageBean groupMessageBean;
    /**
     * Retrieves a list of group messages for a specific project.
     *
     * @param projectId the ID of the project.
     * @param securityContext the security context of the authenticated user.
     * @return a list of GroupMessageGetDto representing the group messages.
     * @throws UserNotFoundException if the user is not found or does not have permission to access the project.
     */
    @GET
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<GroupMessageGetDto> getGroupMessages(@PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws UserNotFoundException {
        return groupMessageBean.getGroupMessagesByProjectId(projectId, securityContext);
    }

}
