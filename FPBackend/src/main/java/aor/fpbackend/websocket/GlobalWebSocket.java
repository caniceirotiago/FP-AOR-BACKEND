// src/main/java/aor/fpbackend/websocket/GlobalWebSocket.java
package aor.fpbackend.websocket;

import aor.fpbackend.dto.WebSocketMessageDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.enums.QueryParams;
import aor.fpbackend.enums.WebSocketMessageType;
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
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.security.Key;
import java.util.concurrent.ConcurrentHashMap;


@ApplicationScoped
@ServerEndpoint("/ws")
public class GlobalWebSocket {

    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    static Gson gson = GsonSetup.createGson();
    @EJB
    private static SessionDao sessionDao;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String query = session.getQueryString();
        String sessionToken = query.split("=")[1];
        if (isValidSessionToken(sessionToken)) {
            sessions.put(sessionToken, session);
        } else {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid session token"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String query = session.getQueryString();
        String sessionToken = query.split("=")[1];
        sessions.remove(sessionToken);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.FORCED_LOGOUT_FAILED)) {
                System.out.println("Forced logout failed for session token: " + session.getQueryString().split("=")[1]);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public static void sendForcedLogoutRequest(SessionEntity sessionEntity) {
        Session session = sessions.get(sessionEntity.getSessionToken());
        if (session != null && session.isOpen()) {
            try {
                String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.FORCED_LOGOUT, null));
                session.getBasicRemote().sendText(jsonResponse);
                System.out.println("Logout message sent to session token: " + sessionEntity.getSessionToken());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Se sessão global não estiver ligada ao WebSocket, desativar sessão
            sessionEntity.setActive(false);
            sessionDao.merge(sessionEntity);
            System.out.println("Session token not found or closed: " + sessionEntity.getSessionToken());
        }
    }

    private boolean isValidSessionToken(String sessionToken) {
        try {
            Key key = JwtKeyProvider.getKey();
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(sessionToken).getBody();
            SessionEntity sessionEntity = sessionDao.findSessionBySessionToken(sessionToken);
            return sessionEntity != null && sessionEntity.isActive();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
