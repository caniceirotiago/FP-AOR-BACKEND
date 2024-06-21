package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class IndividualMessageGetDto implements Serializable {

    @XmlElement
    private Long id;
    @XmlElement
    private String content;
    @XmlElement
    private UserBasicInfoDto sender;
    @XmlElement
    private UserBasicInfoDto recipient;
    @XmlElement
    private String subject;
    @XmlElement
    private Instant sentAt;
    @XmlElement
    private boolean viewed;


    public IndividualMessageGetDto() {
    }

    public IndividualMessageGetDto(Long id, String content, UserBasicInfoDto sender, UserBasicInfoDto recipient, String subject, Instant sentAt, boolean viewed) {
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
