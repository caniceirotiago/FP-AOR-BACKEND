package aor.fpbackend.dto;

import java.time.Instant;

public class IndividualMessageGetDto {

    private Long id;

    private String content;

    private UserBasicInfoDto sender;

    private UserBasicInfoDto recipient;

    private String subject;
    private Instant sentAt;
    private boolean viewed;


    public IndividualMessageGetDto() {
    }

    public IndividualMessageGetDto(Long id, String content,UserBasicInfoDto sender, UserBasicInfoDto recipient, String subject, Instant sentAt, boolean viewed) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.sentAt = sentAt;
        this.viewed = viewed;
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


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isViewed() {
        return viewed;
    }
    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public UserBasicInfoDto getSender() {
        return sender;
    }

    public void setSender(UserBasicInfoDto sender) {
        this.sender = sender;
    }

    public UserBasicInfoDto getRecipient() {
        return recipient;
    }

    public void setRecipient(UserBasicInfoDto recipient) {
        this.recipient = recipient;
    }
}
