package aor.fpbackend.entity;

import aor.fpbackend.enums.NotificationTypeENUM;
import aor.fpbackend.enums.convertors.NotificationTypeENUMConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "notification")
public class NotificationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Convert(converter = NotificationTypeENUMConverter.class)
    @Column(name = "type", nullable = false, unique = false, updatable = false)
    private NotificationTypeENUM type;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "date_time", nullable = false)
    private Instant dateTime;
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_message_id", nullable = true)
    private IndividualMessageEntity individualMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private ProjectEntity project;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    private TaskEntity task;

            // Constructors
    public NotificationEntity() {}

    public NotificationEntity(NotificationTypeENUM type, String content, Instant dateTime, UserEntity user) {
        this.type = type;
        this.content = content;
        this.dateTime = dateTime;
        this.user = user;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public NotificationTypeENUM getType() {
        return type;
    }

    public void setType(NotificationTypeENUM type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public IndividualMessageEntity getIndividualMessage() {
        return individualMessage;
    }

    public void setIndividualMessage(IndividualMessageEntity individualMessage) {
        this.individualMessage = individualMessage;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", dateTime=" + dateTime +
                ", user=" + user +
                '}';
    }
}
