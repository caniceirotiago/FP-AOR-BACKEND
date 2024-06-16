package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

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

    public GroupMessageEntity() {}

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
}
