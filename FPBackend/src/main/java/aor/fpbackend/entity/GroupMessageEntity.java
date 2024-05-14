package aor.fpbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.time.Instant;

@Entity
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
