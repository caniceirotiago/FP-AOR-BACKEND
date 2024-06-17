package aor.fpbackend.websocket;



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
import com.google.gson.Gson;

@ApplicationScoped
@ServerEndpoint("/chat/{token}/{receiverUsername}")
public class IndividualMessageWebSocket {
//    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
//    Gson gson = GsonSetup.createGson();
//    @EJB
//    private MessageBean messageBean;
//    @Inject
//    private UserBean userBean;
//    @EJB
//    private NotificationBean notificationBean;
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam("token") String token, @PathParam("receiverUsername") String receiverUsername) {
//        System.out.println("Chat WebSocket connection opened");
//        try {
//            boolean validated = userBean.tokenValidator(token);
//            String username = validated ? userBean.getUserByToken(token).getUsername() : null;
//            if (username != null) {
//                session.getUserProperties().put("receiverUsername", receiverUsername);
//                session.getUserProperties().put("username", username);
//                session.getUserProperties().put("token", token);
//                userSessions.put(username, session);
//                System.out.println("Chat WebSocket connection opened for user: " + username + " with receiver: " + receiverUsername);
//            } else {
//                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @OnClose
//    public void onClose(Session session, @PathParam("token") String token) {
//        String username = userBean.getUserByToken(token).getUsername();
//        userSessions.remove(username);
//        System.out.println("Chat WebSocket connection closed for user: " + username);
//    }
//
//    @OnError
//    public void onError(Session session, Throwable throwable) {
//        System.out.println("WebSocket error: " + throwable.getMessage());
//    }
//
//    @OnMessage
//    public void onMessage(Session session, String message) {
//        //On message receive two types of messages: markAsRead and sendMessage
//        //markAsRead: mark messages as read
//        //sendMessage: persists message in database and sends it to the receiver if the receiver is online
//        try {
//            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
//            String type = json.get("type").getAsString();
//            if (type.equals("markAsRead")) {
//                markAsRead(json);
//            }
//            else if (type.equals("sendMessage")) {
//                receiveSendMessage(session, json);
//            }
//        } catch (Exception e) {
//            System.err.println("Error processing message: " + e.getMessage());
//        }
//    }
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
//    public void receiveSendMessage(Session session, JsonObject json) throws IOException, UserNotFoundException, DatabaseOperationException {
//        JsonObject data = json.getAsJsonObject("data");
//        MessageDto msg = gson.fromJson(data, MessageDto.class);
//        msg.setSentAt(LocalDateTime.now());
//        if (data != null) {
//            MessageEntity savedMessage = messageBean.sendMessage(msg);
//            MessageDto savedMessageDto = messageBean.convertEntityToDto(savedMessage);
//            System.out.println("Message saved: " + savedMessageDto);
//            if (savedMessage != null) {
//                String jsonResponse = gson.toJson(new WebSocketMessage("receivedMessage", savedMessageDto));
//                Session receiverSession = userSessions.get(savedMessage.getReceiver().getUsername());
//                boolean tokenValidationOnSend = userBean.tokenValidator(session.getUserProperties().get("token").toString());
//                if (receiverSession != null && receiverSession.isOpen() &&
//                        receiverSession.getUserProperties().get("receiverUsername").equals(savedMessageDto.getSenderUsername())  &&
//                        tokenValidationOnSend) {
//                    receiverSession.getBasicRemote().sendText(jsonResponse);
//                }
//                else {
//                    System.out.println("Receiver session is null or closed");
//                    notificationBean.createNotification(savedMessage.getReceiver().getUsername(), "message",
//                            savedMessageDto.getSenderUsername());
//                }
//                if (session.isOpen()) {
//                    session.getBasicRemote().sendText(jsonResponse);
//                }
//            }
//        }
//    }
}
