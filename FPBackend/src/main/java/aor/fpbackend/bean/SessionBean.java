package aor.fpbackend.bean;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.User.UserLoginDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.utils.JwtKeyProvider;
import aor.fpbackend.utils.PassEncoder;
import aor.fpbackend.websocket.GlobalWebSocket;
import io.jsonwebtoken.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;
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

/**
 * SessionBean is a stateless EJB that manages user sessions, including creation, validation,
 * and cleanup of session tokens. It handles tasks such as generating JWT tokens, validating
 * authentication and session tokens, and managing session expiration and invalidation.
 * <p>
 * This bean interacts with various DAOs to fetch user and session data and relies on utility
 * classes for JWT operations and websocket communication.
 * </p>
 * <p>
 * Technologies Used:
 * <ul>
 *     <li><b>Java EE</b>: For building the EJB and managing transactions.</li>
 *     <li><b>JWT</b>: For token generation and validation via {@link JwtKeyProvider}.</li>
 *     <li><b>Jakarta EE</b>: For dependency injection and scheduling.</li>
 *     <li><b>SLF4J</b>: For logging operations.</li>
 * </ul>
 * </p>
 * <p>
 * Dependencies are injected using the {@link EJB} annotation, which includes DAOs for user,
 * session, and role entities. The bean also uses utility classes for JWT operations and
 * websocket communication.
 * </p>
 */
@Stateless
public class SessionBean implements Serializable {
    @EJB
    SessionDao sessionDao;
    @EJB
    UserDao userDao;
    @EJB
    RoleDao roleDao;
    @EJB
    ConfigurationBean configurationBean;
    @EJB
    PassEncoder passEncoder;


    private static final long serialVersionUID = 1L;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionBean.class);


    /**
     * Authenticates a user and creates a new session.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates the user's email and password.</li>
     *     <li>Generates JWT tokens for authentication and session management.</li>
     *     <li>Creates cookies to store the tokens and persists the session information.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Technologies Used:
     * <ul>
     *     <li><b>BCrypt</b>: For password hashing and validation via `PassEncoder`.</li>
     *     <li><b>JWT</b>: For token generation and validation via `SessionBean`.</li>
     *     <li><b>Cookie html only</b>: For storing the tokens in the client's browser.</li>
     * </ul>
     * </p>
     *
     * @param userLogin the DTO containing the user's login credentials.
     * @return a Response containing the authentication and session cookies.
     * @throws InvalidCredentialsException if the email or password is incorrect.
     */
    public Response login(UserLoginDto userLogin) throws InvalidCredentialsException {
        // Find the user by email
        UserEntity userEntity = userDao.findUserByEmail(userLogin.getEmail());
        // Check if user exists and password matches
        if (userEntity == null || !passEncoder.matches(userLogin.getPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        // Get session timeout from configuration entity
        Integer definedTimeOut = configurationBean.getConfigValueByKey("sessionTimeout");
        if(definedTimeOut == null) {
            throw new IllegalStateException("Session timeout not defined");
        }
        try{
            // Generate tokens and calculate expiration time
            Instant now = Instant.now();
            Instant expirationInstant = now.plus(Duration.ofMillis(definedTimeOut));
            String authToken = generateJwtToken(userEntity, definedTimeOut, "auth");
            int cookieExpiration = (int) Duration.ofMillis(definedTimeOut).getSeconds();
            // Create authentication and session cookies
            NewCookie authCookie = new NewCookie("authToken", authToken, "/", null, "Auth Token", cookieExpiration, true, true);
            String sessionToken = generateJwtToken(userEntity, definedTimeOut, "session");
            NewCookie sessionCookie = new NewCookie("sessionToken", sessionToken, "/", null, "Session Token", cookieExpiration, true, false);
            // Persist the new session in the database
            sessionDao.persist(new SessionEntity(authToken, sessionToken, expirationInstant, userEntity));
            LOGGER.info("Successful login new session created");
            // Return the response with the cookies
            return Response.ok().cookie(authCookie).cookie(sessionCookie).build();
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InputValidationException e) {
            throw new RuntimeException(e);
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Logs out a user by invalidating their session token.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the authenticated user's details from the security context.</li>
     *     <li>Invalidates the session associated with the user.</li>
     *     <li>Logs the logout operation for auditing purposes.</li>
     * </ul>
     * </p>
     * @param securityContext the security context containing the authenticated user's details.
     * @throws UserNotFoundException if the token is invalid or the session cannot be found.
     */
    public void logout(SecurityContext securityContext) throws UserNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        // Check if authentication details are valid
        if (authUserDto == null || authUserDto.getToken() == null || authUserDto.getToken().isEmpty()) {
            throw new UserNotFoundException("Invalid or missing token");
        }
        try{
            // Invalidate session in the database using the authentication token
            sessionDao.inativateSessionbyAuthToken(authUserDto.getToken());
            LOGGER.info("Successful logout");
        } catch (NoResultException e) {
            LOGGER.error("Error while logging out user at: " + e.getMessage());
            throw new UserNotFoundException("Error while logging out user");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Scheduled method to clean up expired sessions and manage user sessions.
     * <p>
     * This method is executed every minute to check for user sessions that are about to expire within the next three minutes
     * or have already expired. The primary goal is to ensure that sessions are properly managed, thereby enhancing security
     * and user experience. The method performs the following tasks:
     * <ul>
     *     <li>Fetches sessions that are expiring in the next three minutes or have already expired but are still active.</li>
     *     <li>Logs the number of such sessions for monitoring purposes.</li>
     *     <li>For each session:
     *         <ul>
     *             <li>If the session is expiring in one minute or less, it deactivates the session in the backend, assuming
     *             that the user does not have an active WebSocket session.</li>
     *             <li>If the session is expiring in more than one minute but less than three minutes, it sends a forced logout
     *             request via WebSocket. This prompts the frontend to initiate a REST request that invalidates the authentication cookie.
     *             (html only)
     *             </li>
     *         </ul>
     *     </li>
     * </ul>
     * This approach ensures that:
     * <ul>
     *     <li>Active sessions that are about to expire are properly closed, preventing unauthorized access.</li>
     *     <li>Users are notified of their session's imminent expiration and prompted to re-authenticate, improving security and user experience.</li>
     * </ul>
     * </p>
     */
    @Transactional
    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredTokens() throws DatabaseOperationException {
        // Find sessions expiring within 3 minutes
        List<SessionEntity> sessions = sessionDao.findSessionsExpiringInThreeMinutes();
        Instant now = Instant.now();
        // Iterate through each session
        for (SessionEntity session : sessions) {
            Instant expirationTime = session.getTokenExpiration();
            // Calculate minutes until expiration
            long minutesUntilExpiration = ChronoUnit.MINUTES.between(now, expirationTime);
            // Check if session is within 1 minute of expiration
            if (minutesUntilExpiration <= 1) {
                if (session.isActive()) {
                    try {
                        // Mark session as inactive in the database
                        session.setActive(false);
                        sessionDao.merge(session);
                        LOGGER.info("Session inactivated: " + session.getId());
                    } catch (PersistenceException e) {
                        throw new DatabaseOperationException("Error inactivating session: " + e.getMessage());
                    } finally {
                        ThreadContext.clearMap();
                    }
                }
            } else {
                // Session has expired
                if (session.isActive()) {
                    GlobalWebSocket.sendForcedLogoutRequest(session);
                    LOGGER.info("Forced logout sent for session: " + session.getId());
                    ThreadContext.clearMap();
                }
            }
        }
    }

    /**
     * Generates a JSON Web Token (JWT) for the given user with specified expiration time and token type.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Generates a new UUID as the token ID.</li>
     *     <li>Sets the issued date to the current date.</li>
     *     <li>Sets the expiration date based on the current time and the provided expiration time.</li>
     *     <li>Adds a custom claim for the token type.</li>
     *     <li>Signs the token with a secret key using the HS512 algorithm.</li>
     *     <li>Adds user-specific claims if the user is not null, including user ID, username, and role.</li>
     * </ul>
     * </p>
     *
     * @param user the user entity for whom the token is being generated. Can be null.
     * @param expirationTime the expiration time in milliseconds for the token.
     * @param tokenType the type of the token being generated (e.g., "auth", "session").
     * @return a compact representation of the JWT.
     * @throws UserNotFoundException if the user entity is null.
     * @throws InputValidationException if the expiration time is less than or equal to 0 or the token type is empty.
     */
    public String generateJwtToken(UserEntity user, long expirationTime, String tokenType) throws UserNotFoundException, InputValidationException {
        // Validate input parameters
        if(user == null) {
            throw new UserNotFoundException("User cannot be null on token generation");
        }
        if(expirationTime <= 0 || tokenType == null || tokenType.isEmpty()) {
            throw new InputValidationException("Expiration time must be greater than 0 or token type cannot be empty");
        }
        // Retrieve secret key for signing JWT
        Key secretKey = JwtKeyProvider.getKey();
        // Build JWT token
        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("type", tokenType)
                .signWith(secretKey, SignatureAlgorithm.HS512);
        // Add user-specific claims to the token
        if (user != null) {
            builder.setSubject(String.valueOf(user.getId()));
            builder.claim("username", user.getUsername());
            builder.claim("role", user.getRole().getName());
        }
        try {
            // Generate and return the compact representation of the JWT token
            return builder.compact();
        } catch (Exception e) {
            LOGGER.error("Error generating JWT token", e);
            throw new RuntimeException("Error generating JWT token", e);
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Generates a new secure random token.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Creates an instance of {@link SecureRandom} to generate secure random bytes.</li>
     *     <li>Generates a byte array of 24 random bytes.</li>
     *     <li>Encodes the random bytes into a URL-safe Base64 string.</li>
     * </ul>
     * </p>
     *
     * @return a new secure random token as a URL-safe Base64 encoded string.
     */
    public String generateNewToken() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            Base64.Encoder base64Encoder = Base64.getUrlEncoder();
            byte[] randomBytes = new byte[24];
            secureRandom.nextBytes(randomBytes);
            return base64Encoder.encodeToString(randomBytes);
        } catch (Exception e) {
            LOGGER.error("Error generating new token", e);
            throw new RuntimeException("Error generating new token", e);
        }finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Validates a JSON Web Token (JWT) and retrieves the authenticated user's details.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the secret key is configured.</li>
     *     <li>Finds the session associated with the provided token and validates its status.</li>
     *     <li>Parses the JWT and extracts the claims.</li>
     *     <li>Retrieves the user's details from the database and constructs an {@link AuthUserDto} object.</li>
     * </ul>
     * </p>
     *
     * @param token the JWT to be validated.
     * @return an {@link AuthUserDto} containing the authenticated user's details.
     * @throws InvalidCredentialsException if the token is invalid, expired, or if any error occurs during processing.
     */
    public AuthUserDto validateAuthTokenAndGetUserDetails(String token) throws InvalidCredentialsException {
        Key secretKey = JwtKeyProvider.getKey();
        if (secretKey == null) {
            throw new InvalidCredentialsException("Secret key not configured");
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
        try {
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jwsClaims.getBody();
            Long userId = Long.parseLong(claims.getSubject());

            UserEntity user = userDao.findUserById(userId);
            AuthUserDto authUserDto = new AuthUserDto(user.getId(), user.getRole().getId(), roleDao.findPermissionsByRoleId(user.getRole().getId()), token, session.getId(), user.getUsername());
            return authUserDto;

        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Error processing token: " + e.getMessage());
        }
    }


    /**
     * Validates a session token and retrieves the authenticated user's details.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the secret key is configured.</li>
     *     <li>Finds the session associated with the provided token and validates its status.</li>
     *     <li>Parses the JWT and extracts the claims.</li>
     *     <li>Retrieves the user's details from the database and constructs an {@link AuthUserDto} object.</li>
     * </ul>
     * </p>
     *
     * @param token the session token to be validated.
     * @return an {@link AuthUserDto} containing the authenticated user's details.
     * @throws InvalidCredentialsException if the token is invalid, expired, or if any error occurs during processing.
     */
    public AuthUserDto validateSessionTokenAndGetUserDetails(String token) throws InvalidCredentialsException {
        // Retrieve secret key for JWT verification
        Key secretKey = JwtKeyProvider.getKey();
        // Ensure secret key is configured
        if (secretKey == null) {
            throw new InvalidCredentialsException("Secret key not configured");
        }
        // Find session information from token in the database
        SessionEntity session = sessionDao.findSessionBySessionToken(token);
        // Validate session existence and status
        if (session == null) {
            throw new InvalidCredentialsException("Invalid token");
        }
        if (!session.isActive()) {
            throw new InvalidCredentialsException("Token inativated");
        }
        if (session.getTokenExpiration().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Token expired");
        }
        try {
            // Parse and verify JWT claims using the secret key
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            // Extract user ID from JWT claims
            Claims claims = jwsClaims.getBody();
            Long userId = Long.parseLong(claims.getSubject());
            // Retrieve user details from database using user ID
            UserEntity user = userDao.findUserById(userId);
            // Create AuthUserDto containing user details and token information
            AuthUserDto authUserDto = new AuthUserDto(
                    user.getId(),
                    user.getRole().getId(),
                    roleDao.findPermissionsByRoleId(user.getRole().getId()),
                    token,
                    session.getId(),
                    user.getUsername());
            return authUserDto;
        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Error processing token: " + e.getMessage());
        }
    }

    /**
     * Creates a new session for the user and invalidates the old session.
     * <p>
     * This method is called when a session is reactivated due to user activity before the session expires.
     * It performs the following steps:
     * <ul>
     *     <li>Finds the user entity based on the user ID from the authenticated user DTO.</li>
     *     <li>Generates new authentication and session tokens for the user.</li>
     *     <li>Calculates the expiration time for the new session.</li>
     *     <li>Persists the new session in the database.</li>
     *     <li>Sets the new tokens in the request context properties.</li>
     *     <li>Invalidates the old session token.</li>
     * </ul>
     * </p>
     *
     * @param authUserDto the authenticated user DTO containing user details.
     * @param requestContext the container request context to set new tokens.
     * @param definedTimeout the defined timeout duration for the session.
     * @param oldToken the old session token to be invalidated.
     * @throws UserNotFoundException if the user is not found in the database.
     * @throws InputValidationException if there is an error with the input validation.
     * @throws DatabaseOperationException if there is an error persisting the new session.
     */
    @Transactional
    public void createNewSessionAndInvalidateOld(AuthUserDto authUserDto, ContainerRequestContext requestContext, long definedTimeout, String oldToken) throws UserNotFoundException, InputValidationException, DatabaseOperationException {
        // Retrieve user details from database using user ID in authUserDto
        UserEntity user = userDao.findUserById(authUserDto.getUserId());
        if(user == null) {
            throw new UserNotFoundException("User not found");
        }
        // Generate new authentication and session tokens for the user
        String newAuthToken = generateJwtToken(user, definedTimeout, "auth");
        String newSessionToken = generateJwtToken(user, definedTimeout, "session");
        // Calculate expiration time for the new session
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(Duration.ofMillis(definedTimeout));
        try{
            // Persist new session entity with generated tokens and expiration time
            sessionDao.persist(new SessionEntity(newAuthToken, newSessionToken, expirationInstant, user));
            // Update request context properties with new tokens
            requestContext.setProperty("newAuthToken", newAuthToken);
            requestContext.setProperty("newSessionToken", newSessionToken);
            // Invalidate old session identified by oldToken
            sessionDao.inativateSessionbyAuthToken(oldToken);
            // Log successful session restoration
            LOGGER.info("Session restored for user: " + user.getUsername());
        } catch (PersistenceException e) {
            throw new DatabaseOperationException("Error creating new session: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error creating new session: " + e.getMessage());
        }finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Invalidates the current session and sets invalid tokens in the request context.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Invalidates the current session token in the database.</li>
     *     <li>Generates invalid tokens (value "null") to replace the current tokens.</li>
     *     <li>Sets the new invalid tokens in the request context properties.</li>
     * </ul>
     * </p>
     *
     * @param authUserDto the authenticated user DTO containing user details.
     * @param requestContext the container request context to set new invalid tokens.
     * @throws UnknownHostException if there is an error retrieving host information.
     */
    public void createInvalidSession(AuthUserDto authUserDto, ContainerRequestContext requestContext) throws UnknownHostException {
       try {
           sessionDao.inativateSessionbyAuthToken(authUserDto.getToken());
           String invalidToken = "null";
           requestContext.setProperty("newAuthToken", invalidToken);
           requestContext.setProperty("newSessionToken", invalidToken);
           LOGGER.info("Session invalidated for user: " + authUserDto.getUsername());
       } catch (Exception e) {
           LOGGER.error("Error invalidating session", e);
           throw new RuntimeException("Error invalidating session", e);
       }finally {
           ThreadContext.clearMap();
       }
    }

    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public void setConfigurationBean(ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }
    public void setPassEncoder(PassEncoder passEncoder) {
        this.passEncoder = passEncoder;
    }

}
