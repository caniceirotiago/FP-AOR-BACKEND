
package aor.fpbackend.websocket;

import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dto.WebSocketMessageDto;
import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.SessionEntity;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@ApplicationScoped
@ServerEndpoint("/ws/group/messages")
public class GroupMessageWebSocket {

    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, String> userSessions = new ConcurrentHashMap<>();
    static Gson gson = GsonSetup.createGson();
    @EJB
    private SessionDao sessionDao;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String query = session.getQueryString();
        String sessionToken = query.split("=")[1];
        if (isValidSessionToken(sessionToken)) {
            sessions.put(sessionToken, session);
            Long userId = getUserIdFromSessionToken(sessionToken);
            userSessions.put(userId, sessionToken);
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
        Long userId = getUserIdFromSessionToken(sessionToken);
        userSessions.remove(userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.GROUP_MESSAGE.toString())) {
                System.out.println("group_Message");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void broadcastGroupMessage(GroupMessageEntity message) {
        Long groupId = message.getGroup().getId();
        List<SessionEntity> groupSessions = sessionDao.findActiveSessionsByProjectId(groupId);

        for (SessionEntity sessionEntity : groupSessions) {
            Session session = sessions.get(sessionEntity.getSessionToken());
            if (session != null && session.isOpen()) {
                try {
                    String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.GROUP_MESSAGE.toString(), message));
                    session.getBasicRemote().sendText(jsonResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    private Long getUserIdFromSessionToken(String sessionToken) {
        Key key = JwtKeyProvider.getKey();
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(sessionToken).getBody();
        return Long.parseLong(claims.getSubject());
    }
}
