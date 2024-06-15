package aor.fpbackend.service;

import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("/individual/message")
public class IndividualMessageService {
    @EJB
    IndividualMessageBean individualMessageBean;

    @POST
    @Path("")
    public void sendIndividualMessage(@Valid IndividualMessageSendDto individualMessageSendDto) throws UserNotFoundException {
        individualMessageBean.sendIndividualMessage(individualMessageSendDto);
    }
    @GET
    @Path("/{senderId}/{recipientId}")
    public List<IndividualMessageGetDto> getIndividualMessages(@PathParam("senderId") String senderId, @PathParam("recipientId") String recipientId){
        return individualMessageBean.getIndividualMessages(senderId, recipientId);
    }
}
