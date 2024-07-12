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
/**
 * NotificationService is a JAX-RS resource class that provides RESTful endpoints for managing notifications,
 * including retrieving unread notifications and marking notifications as read.
 */
@Path("/notifications")
public class NotificationService {
    @EJB
    NotificationBean notificationBean;
    /**
     * Retrieves a list of unread notifications for the authenticated user.
     *
     * @param securityContext the security context.
     * @return a list of NotificationGetDto representing unread notifications.
     */
    @GET
    @Path("")
    @Produces("application/json")
    @RequiresMethodPermission(MethodEnum.GET_NOTIFICATIONS)
    public List<NotificationGetDto> getNotifications(@Context SecurityContext securityContext) {
        return notificationBean.getUnreadNotifications(securityContext);
    }

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId the ID of the notification to be marked as read.
     * @throws EntityNotFoundException if the notification is not found.
     */
    @PUT
    @Path("/mark/as/read/{notificationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresMethodPermission(MethodEnum.MARK_NOTIFICATIONS_AS_READ)
    public void markNotificationsAsRead( @PathParam("notificationId") Long notificationId) throws EntityNotFoundException {
        notificationBean.markNotificationsAsRead(notificationId);
    }

}
