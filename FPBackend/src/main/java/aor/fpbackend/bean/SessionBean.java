package aor.fpbackend.bean;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.utils.JwtKeyProvider;
import aor.fpbackend.websocket.GlobalWebSocket;
import io.jsonwebtoken.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Stateless
public class SessionBean implements Serializable {
    @EJB
    SessionDao sessionDao;
    @EJB
    UserDao userDao;
    @EJB
    RoleDao roleDao;

    private static final long serialVersionUID = 1L;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionBean.class);

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredTokens() throws UnknownHostException {
        ThreadContext.put("ip", InetAddress.getLocalHost().getHostAddress());
        List<SessionEntity> sessions = sessionDao.findSessionsExpiringInThreeMinutes();
        Instant now = Instant.now();

        for (SessionEntity session : sessions) {
            Instant expirationTime = session.getTokenExpiration();
            long minutesUntilExpiration = ChronoUnit.MINUTES.between(now, expirationTime);

            if (minutesUntilExpiration <= 1) {
                if (session.isActive()) {
                    session.setActive(false);
                    sessionDao.merge(session);
                    LOGGER.info("Session inactivated: " + session.getId());
                }
            } else {
                if (session.isActive()) {
                    GlobalWebSocket.sendForcedLogoutRequest(session);
                    LOGGER.info("Forced logout sent for session: " + session.getId());
                }
            }
        }
        ThreadContext.clearMap();
    }

    public String generateJwtToken(UserEntity user, long expirationTime, String tokenType) {
        Key secretKey = JwtKeyProvider.getKey();

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("type", tokenType) // Adiciona um claim para o tipo de token
                .signWith(secretKey, SignatureAlgorithm.HS512);

        if (user != null) {
            builder.setSubject(String.valueOf(user.getId()));
            builder.claim("username", user.getUsername());
            builder.claim("role", user.getRole().getName());
        }

        return builder.compact();
    }

    public String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public AuthUserDto validateAuthTokenAndGetUserDetails(String token) throws InvalidCredentialsException {
        try {
            Key secretKey = JwtKeyProvider.getKey();
            if (secretKey == null) {
                throw new IllegalStateException("Secret key not configured");
            }
            SessionEntity session = sessionDao.findSessionByAuthToken(token);
            if (session == null) {
                throw new InvalidCredentialsException("Invalid token");
            }
            if (!session.isActive()) {
                throw new InvalidCredentialsException("Token inativated");
            }
            if (session.getTokenExpiration().isBefore(Instant.now())) {
                throw new InvalidCredentialsException("Token expired");
            }
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jwsClaims.getBody();
            Long userId = Long.parseLong(claims.getSubject());

            UserEntity user = userDao.findUserById(userId);
            AuthUserDto authUserDto = new AuthUserDto(user.getId(), user.getRole().getId(), roleDao.findPermissionsByRoleId(user.getRole().getId()), token);
            return authUserDto;


        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Error processing token: " + e.getMessage());
        }
    }

    public AuthUserDto validateSessionTokenAndGetUserDetails(String token) throws InvalidCredentialsException {
        try {
            Key secretKey = JwtKeyProvider.getKey();
            if (secretKey == null) {
                throw new IllegalStateException("Secret key not configured");
            }
            SessionEntity se = sessionDao.findSessionBySessionToken(token);
            if (se == null) {
                throw new InvalidCredentialsException("Invalid token");
            }
            if (!se.isActive()) {
                throw new InvalidCredentialsException("Token inativated");
            }
            if (se.getTokenExpiration().isBefore(Instant.now())) {
                throw new InvalidCredentialsException("Token expired");
            }
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jwsClaims.getBody();
            Long userId = Long.parseLong(claims.getSubject());
            UserEntity user = userDao.findUserById(userId);
            AuthUserDto authUserDto = new AuthUserDto(user.getId(), user.getRole().getId(), roleDao.findPermissionsByRoleId(user.getRole().getId()), token);
            return authUserDto;
        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Error processing token: " + e.getMessage());
        }
    }

    public void createNewSessionAndInvalidateOld(AuthUserDto authUserDto, ContainerRequestContext requestContext, long definedTimeout, String oldToken) throws UnknownHostException {
        UserEntity user = userDao.findUserById(authUserDto.getUserId());
        String newAuthToken = generateJwtToken(user, definedTimeout, "auth");
        Instant now = Instant.now();
        String newSessionToken = generateJwtToken(user, definedTimeout, "session");
        Instant expirationInstant = now.plus(Duration.ofMillis(definedTimeout));
        sessionDao.persist(new SessionEntity(newAuthToken, newSessionToken, expirationInstant, user));
        requestContext.setProperty("newAuthToken", newAuthToken);
        requestContext.setProperty("newSessionToken", newSessionToken);
        sessionDao.inativateSessionbyAuthToken(oldToken);
    }

    public void createInvalidSession(AuthUserDto authUserDto, ContainerRequestContext requestContext) throws UnknownHostException {
        sessionDao.inativateSessionbyAuthToken(authUserDto.getToken());
        // Gera tokens inválidos (valor "null")
        String invalidToken = "null";
        // Define novos tokens inválidos no contexto da requisição
        requestContext.setProperty("newAuthToken", invalidToken);
        requestContext.setProperty("newSessionToken", invalidToken);
    }
}
