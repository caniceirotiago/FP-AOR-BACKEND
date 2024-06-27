package aor.fpbackend.dto.Interest;

import aor.fpbackend.enums.InterestTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class InterestGetDto implements Serializable {
    @XmlElement
    private long id;
    @XmlElement
    private String name;
    @XmlElement
    @Enumerated
    private InterestTypeEnum type;

    public InterestGetDto() {
    }

    public InterestGetDto(long id, String name, InterestTypeEnum type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InterestTypeEnum getType() {
        return type;
    }

    public void setType(InterestTypeEnum type) {
        this.type = type;
    }
}
