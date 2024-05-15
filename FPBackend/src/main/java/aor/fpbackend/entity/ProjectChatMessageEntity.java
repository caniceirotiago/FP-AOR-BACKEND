package aor.fpbackend.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "project_chat_message")
public class ProjectChatMessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sent_at", nullable = false)
    private Instant creationDate;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity senderUser;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity projectChat;


}
