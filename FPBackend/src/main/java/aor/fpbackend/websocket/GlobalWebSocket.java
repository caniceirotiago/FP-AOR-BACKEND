// src/main/java/aor/fpbackend/websocket/GlobalWebSocket.java
package aor.fpbackend.websocket;

import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.NotificationGetDto;
import aor.fpbackend.dto.WebSocketMessageDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.enums.QueryParams;
import aor.fpbackend.enums.WebSocketMessageType;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.utils.GsonSetup;
import aor.fpbackend.utils.JwtKeyProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@ApplicationScoped
@ServerEndpoint("/ws/{sessionToken}")
public class GlobalWebSocket {

    private static final ConcurrentHashMap<Long, List<Session>> sessions = new ConcurrentHashMap<>();
    static Gson gson = GsonSetup.createGson();
    @EJB
    private static SessionDao sessionDao;
    @EJB
    UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionToken") String sessionToken) {
        System.out.println("Global WebSocket connection opened");
        try {
            AuthUserDto user = userBean.validateSessionTokenAndGetUserDetails(sessionToken);
            if (user != null) {
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                sessions.computeIfAbsent(user.getUserId(), k -> new CopyOnWriteArrayList<>()).add(session);
                System.out.println("Chat WebSocket connection opened for user: " + user.getUserId());
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }

        } catch (InvalidCredentialsException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnClose

    public void onClose(Session session, CloseReason reason) {
        Long userId = (Long) session.getUserProperties().get("userId");
        String sessionToken = (String) session.getUserProperties().get("token");
        List<Session> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
            }
        }
        System.out.println("Chat WebSocket connection closed for user: " + userId + ", session token: " + sessionToken);
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

        if (userSessions != null) {
            Session session = userSessions.stream()
                    .filter(s -> sessionToken.equals(s.getUserProperties().get("token")))
                    .findFirst()
                    .orElse(null);

            if (session != null && session.isOpen()) {
                try {
                    String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.FORCED_LOGOUT.toString(), null));
                    session.getBasicRemote().sendText(jsonResponse);
                    System.out.println("Logout message sent to session token: " + sessionToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Se a sessão não estiver aberta ou não for encontrada, desativar sessão
                sessionEntity.setActive(false);
                sessionDao.merge(sessionEntity);
                System.out.println("Session token not found or closed: " + sessionToken);
            }
        } else {
            // Se não houver sessões para o usuário, desativar sessão
            sessionEntity.setActive(false);
            sessionDao.merge(sessionEntity);
            System.out.println("No active sessions found for user id: " + userId);
        }
    }


    public static void tryToSendNotificationToUserSessions(NotificationGetDto notification) {
        System.out.println("Sending notification to user sessions");
        Long userId = notification.getUser().getId();
        System.out.println("User id: " + userId);
        System.out.println("Current sessions: " + sessions);
        List<Session> userSessions = sessions.get(userId);
        if (userSessions != null) {
            System.out.println("User sessions found: " + userSessions.size());
            for (Session session : userSessions) {
                if (session.isOpen()) {
                    try {
                        System.out.println("Sending notification to session: " + session.getId());
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
