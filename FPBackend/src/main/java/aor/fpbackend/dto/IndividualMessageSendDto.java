package aor.fpbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IndividualMessageSendDto {

    @NotNull
    @NotBlank
    @Size(min = 1, max = 5000)
    private String content;

    @NotNull
    private Long senderId;

    @NotNull
    private Long recipientId;


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
