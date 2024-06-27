package aor.fpbackend.dto;

import aor.fpbackend.entity.IndividualMessageEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.NotificationTypeENUM;
import aor.fpbackend.enums.convertors.NotificationTypeENUMConverter;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Instant;
@XmlRootElement
public class NotificationGetDto {
    @XmlElement
    private long id;
    @XmlElement
    private NotificationTypeENUM type;
    @XmlElement
    private String content;
    @XmlElement
    private Instant dateTime;
    @XmlElement
    private boolean isRead;
    @XmlElement
    private UserBasicInfoDto user;
    @XmlElement
    private IndividualMessageGetDto individualMessage;
    @XmlElement
    private long projectId;

    public NotificationGetDto() {
    }

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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }



    public IndividualMessageGetDto getIndividualMessage() {
        return individualMessage;
    }

    public void setIndividualMessage(IndividualMessageGetDto individualMessage) {
        this.individualMessage = individualMessage;
    }

    public UserBasicInfoDto getUser() {
        return user;
    }

    public void setUser(UserBasicInfoDto user) {
        this.user = user;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
