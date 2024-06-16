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

import java.util.List;

@Path("/group/message")
public class GroupMessageService {
    @EJB
    GroupMessageBean groupMessageBean;

    @POST
    @Path("/{projectId}")
    @RequiresProjectMemberPermission()
    public void sendGroupMessage(@Valid GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException {
        groupMessageBean.sendGroupMessage(groupMessageSendDto);
    }
    @GET
    @Path("/{projectId}")
    @RequiresProjectMemberPermission()
    public List<GroupMessageGetDto> getMessages(@PathParam("projectId") long projectId){
        return groupMessageBean.getGroupMessages(projectId);
    }

    @PUT
    @Path("/read/{projectId}")
    @RequiresProjectMemberPermission()
    public void markMessagesAsRead(GroupMessageMarkReadDto groupMessageMarkReadDto) throws EntityNotFoundException {
        groupMessageBean.markMessageAsRead(groupMessageMarkReadDto);
    }
}
