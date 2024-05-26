package aor.fpbackend.dto;

public class WebSocketMessageDto {
    private String type;
    private Object data;
    public WebSocketMessageDto() {
    }

    public WebSocketMessageDto(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}