package aor.fpbackend.dto.Interest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class InterestRemoveDto implements Serializable {
    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private Long id;

    public InterestRemoveDto() {
    }

    public InterestRemoveDto(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
