package aor.fpbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class UpdateDeletedDto implements Serializable {

    @XmlElement
    private long id;

    @XmlElement
    private String username;

    @XmlElement
    @JsonProperty("isDeleted")
    private boolean isDeleted;

    public UpdateDeletedDto() {
    }

    public UpdateDeletedDto(long id, String username, boolean isDeleted) {
        this.id = id;
        this.username = username;
        this.isDeleted = isDeleted;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}