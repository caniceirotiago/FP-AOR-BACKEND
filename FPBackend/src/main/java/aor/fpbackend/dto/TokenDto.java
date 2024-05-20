package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class TokenDto implements Serializable {

    @XmlElement
    private long id;
    @XmlElement
    private String username;
    @XmlElement
    private String token;
    @XmlElement
    private String photo;
    @XmlElement
    private long roleId;

    public TokenDto() {
    }

    public TokenDto(long id, String username, String token, String photo, long roleId) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.photo = photo;
        this.roleId = roleId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
}
