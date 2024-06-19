
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.lang.reflect.Type;
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
        try {
            AuthUserDto user = userBean.validateSessionTokenAndGetUserDetails(sessionToken);
            if (!projectMembershipDao.isUserProjectMember(projectId, user.getUserId())) {
                throw new UnauthorizedAccessException("User is not a project member");
            }
            if (user != null) {
                session.getUserProperties().put("projectId", projectId);
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                userSessions.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(session);
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
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.NEW_GROUP_MESSAGE.toString())) {
                broadcastGroupMessage(json);
            } else if (type.equals(WebSocketMessageType.MARK_AS_READ.toString())) {
                markAsRead(session, json);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void broadcastGroupMessage(JsonObject json) throws IOException, UserNotFoundException, EntityNotFoundException {
        JsonObject data = json.getAsJsonObject(QueryParams.DATA);
        GroupMessageSendDto msg = gson.fromJson(data, GroupMessageSendDto.class);
        if (data != null) {
            GroupMessageEntity savedGroupMessage = groupMessageBean.sendGroupMessage(msg);
            GroupMessageGetDto savedGroupMessageGetDto = groupMessageBean.convertGroupMessageEntityToGroupMessageGetDto(savedGroupMessage);
            if (savedGroupMessage != null) {
                String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.NEW_GROUP_MESSAGE.toString(), savedGroupMessageGetDto));
                List<Session> groupSessions = userSessions.get(savedGroupMessage.getGroup().getId());
                if (groupSessions != null && !groupSessions.isEmpty()) {
                    for (Session groupSession : groupSessions) {
                        if (groupSession.isOpen() && groupSession.getUserProperties().get("projectId").equals(savedGroupMessageGetDto.getGroupId())) {
                            groupSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                } else {
                    System.out.println("Group session is null or closed");
                }

            }
        }
    }

    public void markAsRead(Session session, JsonObject json) throws IOException {
        JsonElement dataElement = json.get(QueryParams.DATA);
        Type listType = new TypeToken<List<Long>>() {
        }.getType();
        List<Long> messageIds = gson.fromJson(dataElement, listType);


        Long userId = (Long) session.getUserProperties().get("userId");

        for (Long mID : messageIds) {
            System.out.println("Message Id's " + mID + " user Id" + userId);
        }

        boolean success = groupMessageBean.markMessagesAsReadForGroup(messageIds, userId);

        System.out.println("Mark as read WS success " + success);
        if (success) {
            System.out.println("Group messages marked as read: " + messageIds);
            List<GroupMessageGetDto> messages = groupMessageBean.getGroupMessagesByMessageIds(messageIds);
            WebSocketMessageDto response = new WebSocketMessageDto(WebSocketMessageType.MARK_AS_READ.toString(), messages);
            String jsonResponse = gson.toJson(response);
            List<Session> groupSessions = userSessions.get(messages.get(0).getGroupId());
            if (groupSessions != null) {
                for (Session groupSession : groupSessions) {
                    if (groupSession.isOpen()) {
                        groupSession.getBasicRemote().sendText(jsonResponse);
                    }
                }
            }
            List<Session> senderSessions = userSessions.get(messages.get(0).getSender().getId());
            if (senderSessions != null) {
                for (Session senderSession : senderSessions) {
                    if (senderSession.isOpen()) {
                        senderSession.getBasicRemote().sendText(jsonResponse);
                    }
                }
            }
        }
    }

}
