package aor.fpbackend.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
/**
 * JwtKeyProvider provides a singleton {@link Key} instance for JWT signing and verification.
 * <p>
 * This class generates a secret key using the HS512 signature algorithm and provides
 * a method to retrieve this key.
 * </p>
 */
public class JwtKeyProvider {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static Key getKey() {
        return key;
    }
}