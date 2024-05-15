package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "session")

@NamedQuery(name = "Session.findSessionByToken", query = "SELECT s FROM SessionEntity s WHERE s.sessionToken = :tokenValue")
@NamedQuery(name = "Session.findAllSessionsByUserId", query = "SELECT s FROM SessionEntity s WHERE s.user.id = :userId")

public class SessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "last_activity_timestamp", nullable = false)
    private Instant lastActivityTimestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Constructors
    public SessionEntity() {}

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Instant getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public void setLastActivityTimestamp(Instant lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", sessionToken='" + sessionToken + '\'' +
                ", lastActivityTimestamp=" + lastActivityTimestamp +
                ", user=" + user +
                '}';
    }
}
