package aor.fpbackend.dto;

import aor.fpbackend.enums.WebSocketMessageType;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class WebSocketMessageDto implements Serializable {
    @XmlElement
    private String type;

    @XmlElement
    private Object data;

    public WebSocketMessageDto() {
    }

    public WebSocketMessageDto(String type, Object data) {
        this.type = type;
        this.data = data;
    }



    public Object getData() {
        return data;
    }



    public void setData(Object data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }
}