package aor.fpbackend.dto;

import aor.fpbackend.enums.IntKeyTypeEnum;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class KeywordGetDto implements Serializable {

    @XmlElement
    private long id;
    @XmlElement
    private String name;
    @XmlElement
    @Enumerated
    private IntKeyTypeEnum type;

    public KeywordGetDto() {
    }

    public KeywordGetDto(long id, String name, IntKeyTypeEnum type) {
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

    public IntKeyTypeEnum getType() {
        return type;
    }

    public void setType(IntKeyTypeEnum type) {
        this.type = type;
    }
}

