package aor.fpbackend.dto.IndividualMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class IndividualMessageSendDto implements Serializable {

    @XmlElement
    @NotBlank
    @Size(min = 1, max = 5000)
    private String content;
    @XmlElement
    @NotNull
    private Long senderId;
    @XmlElement
    @NotNull
    private Long recipientId;

    @XmlElement
    @Size(max = 255)
    private String subject;

    public IndividualMessageSendDto() {
    }

    public IndividualMessageSendDto(String content, Long senderId, Long recipientId, String subject) {
        this.content = content;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.subject = subject;
    }

    // Getters and setters

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

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}