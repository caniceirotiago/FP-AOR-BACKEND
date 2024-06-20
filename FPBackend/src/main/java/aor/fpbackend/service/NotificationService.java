package aor.fpbackend.service;

import aor.fpbackend.bean.NotificationBean;
import aor.fpbackend.dto.NotificationGetDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/notifications")
public class NotificationService {
    @EJB
    private NotificationBean notificationBean;
    @GET
    @Produces("application/json")
    @RequiresMethodPermission(MethodEnum.GET_NOTIFICATIONS)
    public List<NotificationGetDto> getNotifications(@Context SecurityContext securityContext) {
        return notificationBean.getUnreadNotifications(securityContext);
    }
    @PUT
    @Produces("application/json")
    @RequiresMethodPermission(MethodEnum.MARK_NOTIFICATIONS_AS_READ)
    @Path("/mark/as/read/{notificationId}")
    public void markNotificationsAsRead( @PathParam("notificationId") Long notificationId){
        notificationBean.markNotificationsAsRead(notificationId);
    }

}
