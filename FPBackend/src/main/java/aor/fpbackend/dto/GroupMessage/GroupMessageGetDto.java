package aor.fpbackend.dto.GroupMessage;

import aor.fpbackend.dto.User.UserBasicInfoDto;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class GroupMessageGetDto implements Serializable {

    @XmlElement
    private long messageId;

    @XmlElement
    private String content;

    @XmlElement
    private UserBasicInfoDto sender;

    @XmlElement
    private Instant sentTime;

    @XmlElement
    private Set<Long> readByUserIds;

    @XmlElement
    private boolean isViewed;

    @XmlElement
    private long groupId;


    public GroupMessageGetDto() {
    }

    public GroupMessageGetDto(long messageId, String content, UserBasicInfoDto sender, Instant sentTime, Set<Long> readByUserIds, boolean isViewed, long groupId) {
        this.messageId = messageId;
        this.content = content;
        this.sender = sender;
        this.sentTime = sentTime;
        this.readByUserIds = readByUserIds;
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

    public UserBasicInfoDto getSender() {
        return sender;
    }

    public void setSender(UserBasicInfoDto sender) {
        this.sender = sender;
    }

    public Instant getSentTime() {
        return sentTime;
    }

    public void setSentTime(Instant sentTime) {
        this.sentTime = sentTime;
    }

    public Set<Long> getReadByUserIds() {
        return readByUserIds;
    }

    public void setReadByUserIds(Set<Long> readByUserIds) {
        this.readByUserIds = readByUserIds;
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
