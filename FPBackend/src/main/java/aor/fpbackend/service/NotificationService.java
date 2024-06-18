package aor.fpbackend.service;

import aor.fpbackend.bean.NotificationBean;
import aor.fpbackend.dto.NotificationGetDto;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.filters.RequiresMethodPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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

}
