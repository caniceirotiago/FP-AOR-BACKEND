package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class TokenDto implements Serializable {

    @XmlElement
    private long id;
    @XmlElement
    private String email;
    @XmlElement
    private String nickname;
    @XmlElement
    private String token;
    @XmlElement
    private String photo;

    public TokenDto() {
    }

    public TokenDto(long id, String email, String nickname, String token, String photo) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.token = token;
        this.photo = photo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
}
