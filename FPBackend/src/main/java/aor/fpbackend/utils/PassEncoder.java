package aor.fpbackend.utils;

import jakarta.ejb.Stateless;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;

@Stateless
public class PassEncoder implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int LOG_ROUNDS = 12;

    public String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
