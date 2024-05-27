package aor.fpbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InterestAddDto {

    @XmlElement
    @NotBlank
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z].*", message = "Name must start with a letter")
    private String name;

    public InterestAddDto() {
    }

    public InterestAddDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
