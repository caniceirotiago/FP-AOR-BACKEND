
package aor.fpbackend.websocket;

import aor.fpbackend.bean.GroupMessageBean;
import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.GroupMessageEntity;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.enums.QueryParams;
import aor.fpbackend.enums.WebSocketMessageType;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UnauthorizedAccessException;
import aor.fpbackend.exception.UserNotFoundException;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@ApplicationScoped
@ServerEndpoint("/groupChat/{sessionToken}/{projectId}")
public class GroupMessageWebSocket {

    private static final Map<Long, List<Session>> userSessions = new ConcurrentHashMap<>();
    Gson gson = GsonSetup.createGson();
    @EJB
    private GroupMessageBean groupMessageBean;
    @EJB
    private UserBean userBean;
    @EJB
    private ProjectMembershipDao projectMembershipDao;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionToken") String sessionToken, @PathParam("projectId") Long projectId) {
        System.out.println("GroupChat WebSocket connection opened");
        try {
            AuthUserDto user = userBean.validateSessionTokenAndGetUserDetails(sessionToken);
            if (!projectMembershipDao.isUserProjectMember(projectId, user.getUserId())) {
                throw new UnauthorizedAccessException("User is not a project member");
            }
            if (user != null) {
                session.getUserProperties().put("projectId", projectId);
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                userSessions.computeIfAbsent(user.getUserId(), k -> new CopyOnWriteArrayList<>()).add(session);
                System.out.println("GroupChat WebSocket connection opened for user: " + user.getUserId() + " and project id: " + projectId);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("sessionToken") String sessionToken) throws InvalidCredentialsException {
        AuthUserDto user = userBean.validateSessionTokenAndGetUserDetails(sessionToken);
        if (user != null) {
            List<Session> sessions = userSessions.get(user.getUserId());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(user.getUserId());
                }
            }
            System.out.println("GroupChat WebSocket connection closed for user id: " + user.getUserId());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.GROUP_MESSAGE.toString())) {
                System.out.println("group_Message");
                broadcastGroupMessage(session, json);
            } else if (type.equals(WebSocketMessageType.MARK_AS_READ)) {
                System.out.println("Implement mark as read method()");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void broadcastGroupMessage(Session session, JsonObject json) throws IOException, UserNotFoundException, EntityNotFoundException {
        JsonObject data = json.getAsJsonObject("data");
        GroupMessageSendDto msg = gson.fromJson(data, GroupMessageSendDto.class);
        if (data != null) {
            GroupMessageEntity savedGroupMessage = groupMessageBean.sendGroupMessage(msg);
            GroupMessageGetDto savedGroupMessageGetDto = groupMessageBean.convertGroupMessageEntityToGroupMessageGetDto(savedGroupMessage);
            System.out.println("Group Message saved: " + savedGroupMessageGetDto);
            if (savedGroupMessage != null) {
                String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.NEW_GROUP_MESSAGE, savedGroupMessageGetDto));
                List<Session> groupSessions = userSessions.get(savedGroupMessage.getGroup().getId());
                List<Session> senderSessions = userSessions.get(savedGroupMessage.getSender().getId());
                if (groupSessions != null && !groupSessions.isEmpty()) {
                    for (Session groupSession : groupSessions) {
                        if (groupSession.isOpen() && groupSession.getUserProperties().get("projectId").equals(savedGroupMessageGetDto.getSender().getId())) {
                            groupSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                } else {
                    System.out.println("Group session is null or closed");
                }
                if (senderSessions != null && !senderSessions.isEmpty()) {
                    for (Session senderSession : senderSessions) {
                        if (senderSession.isOpen()) {
                            senderSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                }
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(jsonResponse);
                }
            }
        }
    }

}
