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

@Path("/group/messages")
public class GroupMessageService {
    @EJB
    GroupMessageBean groupMessageBean;

    @GET
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresProjectMemberPermission()
    public List<GroupMessageGetDto> getGroupMessages(@PathParam("projectId") long projectId, @Context SecurityContext securityContext) throws UserNotFoundException {
        return groupMessageBean.getGroupMessagesByProjectId(projectId, securityContext);
    }

    //TODO
    // Method just to debug (To erase later)
//    @POST
//    @Path("/{projectId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @RequiresProjectMemberPermission()
//    public void sendGroupMessage(@PathParam("projectId") long projectId, @Valid GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException, InputValidationException {
//        if (projectId != groupMessageSendDto.getGroupId()) {
//            throw new InputValidationException("Invalid input Id: " + groupMessageSendDto.getGroupId());
//        }
//        groupMessageBean.sendGroupMessage(groupMessageSendDto);
//    }

}
