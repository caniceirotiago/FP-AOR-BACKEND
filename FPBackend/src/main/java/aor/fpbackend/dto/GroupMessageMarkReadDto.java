package aor.fpbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class GroupMessageMarkReadDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long messageId;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long groupId;


    public GroupMessageMarkReadDto() {
    }

    public GroupMessageMarkReadDto( long messageId, long groupId) {
        this.messageId = messageId;
        this.groupId = groupId;
    }

    // Getters and setters


    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
