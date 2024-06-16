package aor.fpbackend.service;

import aor.fpbackend.bean.GroupMessageBean;
import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.dto.*;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.filters.RequiresProjectMemberPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/group/messages")
public class GroupMessageService {
    @EJB
    GroupMessageBean groupMessageBean;

    @POST
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void sendGroupMessage(@Valid GroupMessageSendDto groupMessageSendDto, @Context SecurityContext securityContext) throws UserNotFoundException, EntityNotFoundException {
        groupMessageBean.sendGroupMessage(groupMessageSendDto, securityContext);
    }
    @GET
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<GroupMessageGetDto> getGroupMessages(@PathParam("projectId") long projectId) {
        return groupMessageBean.getGroupMessages(projectId);
    }

    @PUT
    @Path("/read/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public void markMessagesAsRead(GroupMessageMarkReadDto groupMessageMarkReadDto) throws EntityNotFoundException {
        groupMessageBean.markMessageAsRead(groupMessageMarkReadDto);
    }
}
