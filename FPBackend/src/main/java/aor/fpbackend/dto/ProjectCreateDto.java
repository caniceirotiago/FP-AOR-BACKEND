package aor.fpbackend.dto;

import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;


@XmlRootElement
public class ProjectCreateDto implements Serializable {

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private String motivation;

    @XmlElement
    private ProjectStateEnum state;

    @XmlElement
    private Instant creationDate;

    @XmlElement
    private Instant initialDate;

    @XmlElement
    private Instant finalDate;

    @XmlElement
    private Instant conclusionDate;

    public ProjectCreateDto() {
    }
}