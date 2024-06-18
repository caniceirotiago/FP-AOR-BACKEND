package aor.fpbackend.enums;

public enum WebSocketMessageType {
    FORCED_LOGOUT,
    FORCED_LOGOUT_FAILED,
    NEW_INDIVIDUAL_MESSAGE,
    NEW_GROUP_MESSAGE,
    GROUP_MESSAGE,
    MARK_AS_READ
}