package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class GroupMessageGetDto implements Serializable {

    @XmlElement
    private Long id;

    @XmlElement
    private String content;

    @XmlElement
    private Long senderId;

    @XmlElement
    private Instant sentTime;

    @XmlElement
    private boolean isViewed;

    @XmlElement
    private Long groupId;


    public GroupMessageGetDto() {
    }

    public GroupMessageGetDto(Long id, String content, Long senderId, Instant sentTime, boolean isViewed, Long groupId) {
        this.id = id;
        this.content = content;
        this.senderId = senderId;
        this.sentTime = sentTime;
        this.isViewed = isViewed;
        this.groupId = groupId;
    }

    // Getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
