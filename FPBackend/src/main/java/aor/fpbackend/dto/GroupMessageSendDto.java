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
    private long groupId;

    public GroupMessageSendDto() {
    }

    public GroupMessageSendDto(String content, long groupId) {
        this.content = content;
        this.groupId = groupId;
    }

    // Getters and setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
