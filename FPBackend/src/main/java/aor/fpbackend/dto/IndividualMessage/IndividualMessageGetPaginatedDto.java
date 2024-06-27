package aor.fpbackend.dto.IndividualMessage;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class IndividualMessageGetPaginatedDto implements Serializable {
    @XmlElement
    private List<IndividualMessageGetDto> messages;
    @XmlElement
    private long totalMessages;

    // Getters and setters
    public List<IndividualMessageGetDto> getMessages() {
        return messages;
    }

    public void setMessages(List<IndividualMessageGetDto> messages) {
        this.messages = messages;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
}
