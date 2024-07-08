// src/main/java/aor/fpbackend/websocket/GlobalWebSocket.java
package aor.fpbackend.websocket;

import aor.fpbackend.bean.SessionBean;
import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Notification.NotificationGetDto;
import aor.fpbackend.dto.Websocket.WebSocketMessageDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.enums.QueryParams;
import aor.fpbackend.enums.WebSocketMessageType;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.utils.GsonSetup;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@ApplicationScoped
@ServerEndpoint("/ws/{sessionToken}")
public class GlobalWebSocket {
    private static final Logger LOGGER = LogManager.getLogger(GlobalWebSocket.class);

    private static final ConcurrentHashMap<Long, List<Session>> sessions = new ConcurrentHashMap<>();
    static Gson gson = GsonSetup.createGson();
    @EJB
    private static SessionDao sessionDao;
    @EJB
    private SessionBean sessionBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionToken") String sessionToken) {
        try {
            AuthUserDto user = sessionBean.validateSessionTokenAndGetUserDetails(sessionToken);
            if (user != null) {
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                sessions.computeIfAbsent(user.getUserId(), k -> new CopyOnWriteArrayList<>()).add(session);
                LOGGER.info("WS Global session opened for user: " + user.getUserId());
            } else {
                LOGGER.warn("User validation failed, user is null");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }
        } catch (InvalidCredentialsException e) {
            LOGGER.error("Invalid session token, the JWT key may have expired: " + e.getMessage());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid session token"));
            } catch (IOException ioException) {
                LOGGER.error("Error closing session after invalid token", ioException);
            }
        } catch (Exception e) {
            LOGGER.error("Error opening WS Global session: " + e.getMessage(), e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Error opening session"));
            } catch (IOException ioException) {
                LOGGER.error("Error closing session after unexpected error", ioException);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            List<Session> userSessions = sessions.get(userId);
            if (userSessions != null) {
                userSessions.remove(session);
                if (userSessions.isEmpty()) {
                    sessions.remove(userId);
                }
                LOGGER.info("WS Global session closed for user: " + userId + " with reason: " + reason.getReasonPhrase());
            } else {
                LOGGER.warn("No sessions found for user id: " + userId);
            }
        } else {
            LOGGER.warn("Session closed but no userId found in session properties");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.FORCED_LOGOUT_FAILED.toString())) {
                System.out.println("Forced logout failed for session token: " + session.getQueryString().split("=")[1]);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public static void sendForcedLogoutRequest(SessionEntity sessionEntity) {
        Long userId = sessionEntity.getUser().getId();
        String sessionToken = sessionEntity.getSessionToken();
        List<Session> userSessions = sessions.get(userId);
        System.out.println("User sessions: " + userSessions);
        if (userSessions != null) {
            System.out.println("User sessions size: " + userSessions.size());
            Session session = userSessions.stream()
                    .filter(s -> sessionToken.equals(s.getUserProperties().get("token")))
                    .findFirst()
                    .orElse(null);

            if (session != null && session.isOpen()) {
                try {
                    System.out.println("Sending forced logout request to session: " + session.getId());
                    String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.FORCED_LOGOUT.toString(), null));
                    session.getBasicRemote().sendText(jsonResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // If the session is not open or cannot be found, deactivate the session
                sessionEntity.setActive(false);
                sessionDao.merge(sessionEntity);
            }
        } else {
            // If there are no sessions for the user, deactivate session
            System.out.println("Session not found or not open");
            sessionEntity.setActive(false);
            sessionDao.merge(sessionEntity);
        }
    }

    public static void tryToSendNotificationToUserSessions(NotificationGetDto notification) {
        Long userId = notification.getUser().getId();
        List<Session> userSessions = sessions.get(userId);
        if (userSessions != null) {
            for (Session session : userSessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(gson.toJson(new WebSocketMessageDto(WebSocketMessageType.RECEIVED_NOTIFICATION.toString(), notification)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("No sessions found for user id: " + userId);
        }
    }
}
