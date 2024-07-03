package aor.fpbackend.service;

import aor.fpbackend.bean.NotificationBean;
import aor.fpbackend.dto.Notification.NotificationGetDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/notifications")
public class NotificationService {
    @EJB
    NotificationBean notificationBean;
    @GET
    @Path("")
    @Produces("application/json")
    @RequiresMethodPermission(MethodEnum.GET_NOTIFICATIONS)
    public List<NotificationGetDto> getNotifications(@Context SecurityContext securityContext) {
        return notificationBean.getUnreadNotifications(securityContext);
    }
    @PUT
    @Path("/mark/as/read/{notificationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.MARK_NOTIFICATIONS_AS_READ)
    public void markNotificationsAsRead( @PathParam("notificationId") Long notificationId) throws EntityNotFoundException {
        notificationBean.markNotificationsAsRead(notificationId);
    }

}
