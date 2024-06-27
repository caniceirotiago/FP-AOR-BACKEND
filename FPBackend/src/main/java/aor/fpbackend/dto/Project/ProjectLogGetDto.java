package aor.fpbackend.dto.Project;

import aor.fpbackend.enums.LogTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;

@XmlRootElement
public class ProjectLogGetDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private Instant creationDate;

    @XmlElement
    @Enumerated
    private LogTypeEnum type;

    @XmlElement
    private String content;

    @XmlElement
    private String username;

    @XmlElement
    private String projectName;


    public ProjectLogGetDto() {
    }

    public ProjectLogGetDto(long id, Instant creationDate, LogTypeEnum type, String content, String username, String projectName) {
        this.id = id;
        this.creationDate = creationDate;
        this.type = type;
        this.content = content;
        this.username = username;
        this.projectName = projectName;
    }

    // Getters and setters


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public LogTypeEnum getType() {
        return type;
    }

    public void setType(LogTypeEnum type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
