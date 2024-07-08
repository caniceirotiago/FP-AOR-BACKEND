package aor.fpbackend.websocket;

import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.bean.NotificationBean;
import aor.fpbackend.bean.SessionBean;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessage.IndividualMessageSendDto;
import aor.fpbackend.dto.Websocket.WebSocketMessageDto;
import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.enums.QueryParams;
import aor.fpbackend.enums.WebSocketMessageType;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.utils.GsonSetup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
@ServerEndpoint("/emailChat/{sessionToken}/{receiverId}")
public class IndividualMessageWebSocket {

    private static final Logger LOGGER = LogManager.getLogger(IndividualMessageWebSocket.class);
    private static final Map<Long, List<Session>> userSessions = new ConcurrentHashMap<>();
    Gson gson = GsonSetup.createGson();

    @EJB
    private IndividualMessageBean individualMessageBean;
    @EJB
    private NotificationBean notificationBean;
    @EJB
    private SessionBean sessionBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionToken") String sessionToken, @PathParam("receiverId") Long receiverId) {
        try {
            AuthUserDto user = sessionBean.validateSessionTokenAndGetUserDetails(sessionToken);

            if (user != null) {
                session.getUserProperties().put("receiverId", receiverId);
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                userSessions.computeIfAbsent(user.getUserId(), k -> new CopyOnWriteArrayList<>()).add(session);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("sessionToken") String sessionToken) throws InvalidCredentialsException {
        AuthUserDto user = sessionBean.validateSessionTokenAndGetUserDetails(sessionToken);
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

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    //On message receive two types of messages: markAsRead and sendMessage
    //markAsRead: mark messages as read
    //sendMessage: persists message in database and sends it to the receiver if the receiver is online
    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get(QueryParams.TYPE).getAsString();
            if (type.equals(WebSocketMessageType.MARK_AS_READ.toString())) {
                markAsRead(json);
            } else if (type.equals(WebSocketMessageType.NEW_INDIVIDUAL_MESSAGE.toString())) {
                receiveSendMessage(session, json);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void markAsRead(JsonObject json) throws IOException {
        JsonElement dataElement = json.get(QueryParams.DATA);
        Type listType = new TypeToken<List<Long>>() {
        }.getType();
        List<Long> messageIds = gson.fromJson(dataElement, listType);
        boolean success = individualMessageBean.markMessagesAsRead(messageIds);
        if (success) {
            List<IndividualMessageGetDto> messages = individualMessageBean.getMessagesByIds(messageIds);
            WebSocketMessageDto response = new WebSocketMessageDto(WebSocketMessageType.MARK_AS_READ.toString(), messages);
            String jsonResponse = gson.toJson(response);
            List<Session> receiverSessions = userSessions.get(messages.get(0).getRecipient().getId());
            if (receiverSessions != null) {
                for (Session receiverSession : receiverSessions) {
                    if (receiverSession.isOpen()) {
                        receiverSession.getBasicRemote().sendText(jsonResponse);
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

    public void receiveSendMessage(Session session, JsonObject json) throws IOException, UserNotFoundException {
        JsonObject data = json.getAsJsonObject(QueryParams.DATA);
        IndividualMessageSendDto msg = gson.fromJson(data, IndividualMessageSendDto.class);
        if (data != null) {
            IndividualMessageEntity savedMessage = individualMessageBean.sendIndividualMessage(msg);
            IndividualMessageGetDto savedMessageDto = individualMessageBean.convertToDto(savedMessage);
            if (savedMessage != null) {
                String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.NEW_INDIVIDUAL_MESSAGE.toString(), savedMessageDto));
                List<Session> receiverSessions = userSessions.get(savedMessage.getRecipient().getId());
                List<Session> senderSessions = userSessions.get(savedMessage.getSender().getId());
                if (receiverSessions != null && !receiverSessions.isEmpty()) {
                    for (Session receiverSession : receiverSessions) {
                        if (receiverSession.isOpen() && receiverSession.getUserProperties().get("receiverId").equals(savedMessageDto.getSender().getId())) {
                            receiverSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                } else {
                    notificationBean.createIndividualMessageNotification(savedMessage);
                }
                if (senderSessions != null && !senderSessions.isEmpty()) {
                    for (Session senderSession : senderSessions) {
                        if (senderSession.isOpen()) {
                            senderSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                }
            }
        }
    }
}
