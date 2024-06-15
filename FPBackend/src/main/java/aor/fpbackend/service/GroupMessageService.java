package aor.fpbackend.service;

import aor.fpbackend.bean.GroupMessageBean;
import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.dto.GroupMessageGetDto;
import aor.fpbackend.dto.GroupMessageSendDto;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("/group/message")
public class GroupMessageService {
    @EJB
    GroupMessageBean groupMessageBean;

    @POST
    @Path("")
    public void sendMessage(@Valid GroupMessageSendDto groupMessageSendDto) throws UserNotFoundException, EntityNotFoundException {
        groupMessageBean.sendGroupMessage(groupMessageSendDto);
    }
    @GET
    @Path("/{projectId}")
    public List<GroupMessageGetDto> getMessages(@PathParam("projectId") long projectId){
        return groupMessageBean.getGroupMessages(projectId);
    }
}
