package aor.fpbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.time.Instant;

@Entity
public class IndividualMessageEntity extends MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient;

    public IndividualMessageEntity() {}

    public IndividualMessageEntity(String content, UserEntity sender, Instant sentTime, UserEntity recipient) {
        super(content, sender, sentTime);
        this.recipient = recipient;
    }

    // Getters and setters

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }
}
