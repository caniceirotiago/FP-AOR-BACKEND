package aor.fpbackend.websocket;



import aor.fpbackend.bean.IndividualMessageBean;
import aor.fpbackend.bean.UserBean;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.IndividualMessageGetDto;
import aor.fpbackend.dto.IndividualMessageSendDto;
import aor.fpbackend.dto.WebSocketMessageDto;
import aor.fpbackend.entity.IndividualMessageEntity;
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
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;

@ApplicationScoped
@ServerEndpoint("/emailChat/{sessionToken}/{receiverId}")
public class IndividualMessageWebSocket {
    private static final Map<Long, List<Session>> userSessions = new ConcurrentHashMap<>();
    Gson gson = GsonSetup.createGson();
    @EJB
    private IndividualMessageBean messageBean;
    @EJB
    private UserBean userBean;
    @EJB
    private IndividualMessageBean individualMessageBean;
//    @EJB
//    private NotificationBean notificationBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionToken") String sessionToken, @PathParam("receiverId") Long receiverId) {
        System.out.println("Chat WebSocket connection opened");
        try {
            AuthUserDto user = userBean.validateSessionTokenAndGetUserDetails(sessionToken);

            if (user != null) {
                session.getUserProperties().put("receiverId", receiverId);
                session.getUserProperties().put("userId", user.getUserId());
                session.getUserProperties().put("token", sessionToken);
                userSessions.computeIfAbsent(user.getUserId(), k -> new CopyOnWriteArrayList<>()).add(session);
                System.out.println("Chat WebSocket connection opened for user: " + user.getUserId() + " with receiver: " + receiverId);
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
            System.out.println("Chat WebSocket connection closed for user id: " + user.getUserId());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        //On message receive two types of messages: markAsRead and sendMessage
        //markAsRead: mark messages as read
        //sendMessage: persists message in database and sends it to the receiver if the receiver is online
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            if (type.equals("markAsRead")) {
                //markAsRead(json);
            }
            else if (type.equals("sendMessage")) {
                receiveSendMessage(session, json);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
//    public void markAsRead(JsonObject json) throws IOException {
//        JsonElement dataElement = json.get("data");
//        Type listType = new TypeToken<List<Long>>() {}.getType();
//        List<Long> messageIds = gson.fromJson(dataElement, listType);
//        boolean success = messageBean.markMessagesAsRead(messageIds);
//        if (success) {
//            System.out.println("Messages marked as read: " + messageIds);
//            List<MessageDto> messages = messageBean.getMessagesByIds(messageIds);
//            WebSocketMessage response = new WebSocketMessage("markedAsReadMessages", messages);
//            String jsonResponse = gson.toJson(response);
//            Session receiverSession = userSessions.get(messages.getFirst().getReceiverUsername());
//            Session senderSession = userSessions.get(messages.getFirst().getSenderUsername());
//            if (receiverSession != null && receiverSession.isOpen() &&
//                    receiverSession.getUserProperties().get("receiverUsername").equals(messages.getFirst().getSenderUsername())) {
//                System.out.println("Receiver session open: sending to receiver the marked");
//                receiverSession.getBasicRemote().sendText(jsonResponse);
//            }
//            if (senderSession != null && senderSession.isOpen()) {
//                System.out.println("Sender session open: sending to sender the marked");
//                System.out.println(jsonResponse);
//                senderSession.getBasicRemote().sendText(jsonResponse);
//            }
//        }
//    }
    public void receiveSendMessage(Session session, JsonObject json) throws IOException, UserNotFoundException {
        JsonObject data = json.getAsJsonObject("data");
        IndividualMessageSendDto msg = gson.fromJson(data, IndividualMessageSendDto.class);
        if (data != null) {

            IndividualMessageEntity savedMessage =  individualMessageBean.sendIndividualMessage(msg);
            IndividualMessageGetDto savedMessageDto = individualMessageBean.convertToDto(savedMessage);
            System.out.println("Message saved: " + savedMessageDto);
            if (savedMessage != null) {
                String jsonResponse = gson.toJson(new WebSocketMessageDto(WebSocketMessageType.NEW_INDIVIDUAL_MESSAGE, savedMessageDto));
                List<Session> receiverSessions = userSessions.get(savedMessage.getRecipient().getId());
                List<Session> senderSessions = userSessions.get(savedMessage.getSender().getId());
                if (receiverSessions != null && !receiverSessions.isEmpty()) {
                    for (Session receiverSession : receiverSessions) {
                        if (receiverSession.isOpen() && receiverSession.getUserProperties().get("receiverId").equals(savedMessageDto.getSender().getId())) {
                            receiverSession.getBasicRemote().sendText(jsonResponse);
                        }
                    }
                }else {
                    System.out.println("Receiver session is null or closed");
                    //notificationBean.createNotification(savedMessage.getReceiver().getUsername(), "message",
                         //   savedMessageDto.getSenderUsername());
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
