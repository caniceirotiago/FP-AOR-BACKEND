package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.io.Serializable;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "content",nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @Column(name = "sent_time", nullable = false)
    private Instant sentTime;

    @Column(name = "is_viewed", nullable = false)
    private boolean isViewed;

    // Construtores, getters e setters
    public MessageEntity() {}

    public MessageEntity(String content, UserEntity sender, Instant sentTime) {
        this.content = content;
        this.sender = sender;
        this.sentTime = sentTime;
    }

    // Standard getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
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
}
