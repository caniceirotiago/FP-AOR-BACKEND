package aor.fpbackend.dto.Interest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class InterestRemoveDto implements Serializable {
    @XmlElement
    @NotNull
    @Size
    private long id;

    public InterestRemoveDto() {
    }

    public InterestRemoveDto(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
