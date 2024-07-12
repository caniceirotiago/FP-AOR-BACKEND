package aor.fpbackend.service;

import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetPaginatedDto;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.filters.RequiresPermissionByUserOnIndividualMessage;
import aor.fpbackend.filters.RequiresPermissionByUserOnIndividualMessageAllMessages;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
/**
 * IndividualMessageService is a JAX-RS resource class that provides RESTful endpoints for managing individual messages,
 * including retrieving individual messages between users and filtered messages.
 */
@Path("/individual/messages")
public class IndividualMessageService {
    @EJB
    IndividualMessageBean individualMessageBean;
    /**
     * Retrieves a list of individual messages between a sender and a recipient.
     *
     * @param senderId the ID of the sender.
     * @param recipientId the ID of the recipient.
     * @return a list of IndividualMessageGetDto representing the individual messages.
     * @throws UserNotFoundException if the sender or recipient is not found.
     */
    @GET
    @Path("/{senderId}/{recipientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissionByUserOnIndividualMessage()
    public List<IndividualMessageGetDto> getIndividualMessages(@PathParam("senderId") String senderId, @PathParam("recipientId") String recipientId) throws UserNotFoundException {
        return individualMessageBean.getIndividualMessages(senderId, recipientId);
    }

    /**
     * Retrieves a paginated and filtered list of individual messages for a user.
     *
     * @param userId the ID of the user.
     * @param type the type of messages to filter by.
     * @param page the page number for pagination.
     * @param pageSize the number of messages per page.
     * @param uriInfo URI information for constructing query parameters.
     * @return an IndividualMessageGetPaginatedDto containing the filtered messages.
     * @throws UserNotFoundException if the user is not found.
     * @throws InputValidationException if the input parameters are invalid.
     */
    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissionByUserOnIndividualMessageAllMessages()
    public IndividualMessageGetPaginatedDto getFilteredMessages(
            @QueryParam("userId") String userId,
            @QueryParam("type") String type,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("8") int pageSize,
            @Context UriInfo uriInfo) throws UserNotFoundException, InputValidationException {
        return individualMessageBean.getFilteredMessages(userId, type, page, pageSize, uriInfo);
    }
}
