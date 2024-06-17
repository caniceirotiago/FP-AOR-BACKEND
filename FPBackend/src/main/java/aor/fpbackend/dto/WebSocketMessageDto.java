package aor.fpbackend.dto;

import aor.fpbackend.enums.WebSocketMessageType;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class WebSocketMessageDto implements Serializable {
    @XmlElement
    @Enumerated
    private WebSocketMessageType type;

    @XmlElement
    private Object data;

    public WebSocketMessageDto() {
    }

    public WebSocketMessageDto(WebSocketMessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public WebSocketMessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void setType(WebSocketMessageType type) {
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