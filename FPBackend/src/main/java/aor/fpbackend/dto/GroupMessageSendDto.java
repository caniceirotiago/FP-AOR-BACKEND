package aor.fpbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class GroupMessageSendDto implements Serializable {

    @XmlElement
    @NotNull
    @Size(min = 1, max = 5000)
    private String content;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long senderId;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long groupId;

    public GroupMessageSendDto() {
    }

    public GroupMessageSendDto(String content, long senderId, long groupId) {
        this.content = content;
        this.senderId = senderId;
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
