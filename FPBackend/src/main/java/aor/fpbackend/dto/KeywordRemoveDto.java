package aor.fpbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class KeywordRemoveDto {

    @XmlElement
    @NotNull
    private long id;

    public KeywordRemoveDto() {
    }

    public KeywordRemoveDto(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
