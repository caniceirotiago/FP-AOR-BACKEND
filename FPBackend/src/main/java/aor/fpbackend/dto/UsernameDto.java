package aor.fpbackend.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class UsernameDto implements Serializable {

    @XmlElement
    @NotNull
    private long id;

    @XmlElement
    @NotNull
    private String username;

    public UsernameDto() {
    }

    public UsernameDto(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
