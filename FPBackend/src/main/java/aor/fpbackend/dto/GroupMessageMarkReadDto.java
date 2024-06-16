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
    private Instant sentTime;

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long groupId;


    public GroupMessageMarkReadDto() {
    }

    public GroupMessageMarkReadDto( Instant sentTime, long groupId) {
        this.sentTime = sentTime;
        this.groupId = groupId;
    }

    // Getters and setters

    public Instant getSentTime() {
        return sentTime;
    }

    public void setSentTime(Instant sentTime) {
        this.sentTime = sentTime;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
