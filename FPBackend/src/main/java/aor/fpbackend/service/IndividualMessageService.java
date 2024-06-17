package aor.fpbackend.service;

import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageGetPaginatedDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

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
    public List<IndividualMessageGetDto> getIndividualMessages(@PathParam("senderId") String senderId, @PathParam("recipientId") String recipientId) throws UserNotFoundException {
        return individualMessageBean.getIndividualMessages(senderId, recipientId);
    }
    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    public IndividualMessageGetPaginatedDto getFilteredMessages(
            @QueryParam("userId") String userId,
            @QueryParam("type") String type,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("8") int pageSize,
            @Context UriInfo uriInfo) throws UserNotFoundException {
        return individualMessageBean.getFilteredMessages(userId, type, page, pageSize, uriInfo);
    }
}
