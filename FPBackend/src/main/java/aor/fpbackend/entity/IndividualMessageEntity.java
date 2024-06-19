package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "individual_message")

@NamedQuery(name = "IndividualMessage.findIndividualMessageById", query = "SELECT im FROM IndividualMessageEntity im WHERE im.id = :messageId")

public class IndividualMessageEntity extends MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "subject",nullable = true)
    private String subject;
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient;
    @OneToOne
    @JoinColumn(name = "notification_id", nullable = true)
    private NotificationEntity notification;

    public IndividualMessageEntity() {}

    public IndividualMessageEntity(String content, UserEntity sender, Instant sentTime, UserEntity recipient, String subject) {
        super(content, sender, sentTime);
        this.recipient = recipient;
        this.subject = subject;
    }

    // Getters and setters

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

}
