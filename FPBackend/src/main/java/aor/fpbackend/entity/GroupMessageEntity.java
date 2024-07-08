package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_message")

@NamedQuery(name = "GroupMessage.findGroupMessageById", query = "SELECT gm FROM GroupMessageEntity gm WHERE gm.id = :messageId")
@NamedQuery(name = "GroupMessage.findPreviousGroupMessages", query = "SELECT gm FROM GroupMessageEntity gm WHERE gm.group.id = :projectId AND gm.sentTime <= :sentTime ORDER BY gm.sentTime DESC"
)


public class GroupMessageEntity extends MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ProjectEntity group;

    @OneToMany(mappedBy = "groupMessage", fetch = FetchType.LAZY)
    private Set<NotificationEntity> notifications = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "group_message_read_receipts",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> readByUsers = new HashSet<>();

    public GroupMessageEntity() {}

    public GroupMessageEntity(ProjectEntity group) {
        this.group = group;
    }

    public GroupMessageEntity(String content, UserEntity sender, Instant sentTime, ProjectEntity group) {
        super(content, sender, sentTime);
        this.group = group;
    }

    // Getters and setters

    public ProjectEntity getGroup() {
        return group;
    }

    public void setGroup(ProjectEntity group) {
        this.group = group;
    }

    public Set<UserEntity> getReadByUsers() {
        return readByUsers;
    }

    public void setReadByUsers(Set<UserEntity> readByUsers) {
        this.readByUsers = readByUsers;
    }

    public Set<NotificationEntity> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<NotificationEntity> notifications) {
        this.notifications = notifications;
    }
}
