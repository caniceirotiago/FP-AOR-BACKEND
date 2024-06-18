package aor.fpbackend.enums;

public enum WebSocketMessageType {
    FORCED_LOGOUT("FORCED_LOGOUT"),
    FORCED_LOGOUT_FAILED("FORCED_LOGOUT_FAILED"),
    NEW_INDIVIDUAL_MESSAGE("NEW_INDIVIDUAL_MESSAGE"),
    NEW_GROUP_MESSAGE("NEW_GROUP_MESSAGE"),
    GROUP_MESSAGE("GROUP_MESSAGE"),
    MARK_AS_READ("MARK_AS_READ");

    private final String value;

    WebSocketMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}