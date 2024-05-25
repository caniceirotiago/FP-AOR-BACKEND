package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "session")

@NamedQuery(name = "Session.findSessionByAuthToken", query = "SELECT s FROM SessionEntity s WHERE s.authToken = :tokenValue")
@NamedQuery(name = "Session.findSessionBySessionToken", query = "SELECT s FROM SessionEntity s WHERE s.sessionToken = :tokenValue")

@NamedQuery(name = "Session.findValidSessionByToken",query =
        "SELECT s FROM SessionEntity s WHERE s.authToken = :tokenValue AND s.tokenExpiration > CURRENT_TIMESTAMP")
@NamedQuery(name = "Session.findAllSessionsByUserId", query = "SELECT s FROM SessionEntity s WHERE s.user.id = :userId")
@NamedQuery(name = "Session.findSessionsExpiringInOneMinute", query = "SELECT s FROM SessionEntity s WHERE s.tokenExpiration <= :expirationTime AND s.isActive = true")


public class SessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "auth_token", nullable = false, unique = true, length = 1000)
    private String authToken;
    @Column(name = "session_token", nullable = false, unique = true, length = 1000)
    private String sessionToken;

    @Column(name = "session_token_expiration", nullable = false)
    private Instant tokenExpiration;
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Constructors
    public SessionEntity() {}

    public SessionEntity(String authToken, String sessionToken, Instant tokenExpiration, UserEntity user) {
        this.sessionToken = sessionToken;
        this.tokenExpiration = tokenExpiration;
        this.user = user;
        this.authToken = authToken;
    }

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

    public Instant getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(Instant tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", sessionToken='" + sessionToken + '\'' +
                ", tokenExpiration=" + tokenExpiration +
                ", user=" + user +
                '}';
    }
}
