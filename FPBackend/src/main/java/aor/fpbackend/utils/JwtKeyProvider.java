package aor.fpbackend.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtKeyProvider {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static Key getKey() {
        return key;
    }
}