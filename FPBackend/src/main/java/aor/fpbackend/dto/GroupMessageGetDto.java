package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class GroupMessageGetDto implements Serializable {

    @XmlElement
    private long messageId;

    @XmlElement
    private String content;

    @XmlElement
    private long senderId;

    @XmlElement
    private Instant sentTime;

    @XmlElement
    private boolean isViewed;

    @XmlElement
    private long groupId;


    public GroupMessageGetDto() {
    }

    public GroupMessageGetDto(long messageId, String content, long senderId, Instant sentTime, boolean isViewed, long groupId) {
        this.messageId = messageId;
        this.content = content;
        this.senderId = senderId;
        this.sentTime = sentTime;
        this.isViewed = isViewed;
        this.groupId = groupId;
    }

    // Getters and setters


    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
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

    public Instant getSentTime() {
        return sentTime;
    }

    public void setSentTime(Instant sentTime) {
        this.sentTime = sentTime;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
