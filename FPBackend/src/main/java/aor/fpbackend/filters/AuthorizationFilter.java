package aor.fpbackend.filters;


import aor.fpbackend.bean.ConfigurationBean;
import aor.fpbackend.bean.SessionBean;
import aor.fpbackend.dao.ProjectMembershipDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.entity.ProjectMembershipEntity;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.exception.InputValidationException;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import aor.fpbackend.utils.GlobalSettings;
import aor.fpbackend.utils.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import aor.fpbackend.bean.UserBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * <p>The {@code AuthorizationFilter} class is responsible for handling the authorization of incoming HTTP requests.
 * It intercepts requests, validates authentication tokens, sets up security contexts, and checks user permissions
 * to ensure that only authorized users can access protected resources.</p>
 *
 * <p>This class utilizes several Jakarta EE and Java technologies:</p>
 * <ul>
 *   <li>Jakarta EE annotations such as {@code @Provider} and {@code @Priority} to integrate with the Jakarta REST framework.</li>
 *   <li>EJBs for business logic and session management, including {@code UserBean}, {@code ConfigurationBean}, {@code SessionBean}, and {@code ProjectMembershipDao}.</li>
 *   <li>Contextual dependencies injection using {@code @Context} for accessing HTTP request information and resource metadata.</li>
 *   <li>JSON Web Tokens (JWT) for secure token validation and renewal using the {@code Jwts} class and a custom {@code JwtKeyProvider}.</li>
 *   <li>ThreadContext from Apache Log4j for logging and tracking user sessions across requests.</li>
 * </ul>
 *
 * <p>The main responsibilities of this filter include:</p>
 * <ul>
 *   <li>Identifying public endpoints that do not require authentication using the {@code isPublicEndpoint} method.</li>
 *   <li>Extracting and validating the authentication token from the request headers.</li>
 *   <li>Setting up the security context with user-specific details for authenticated requests.</li>
 *   <li>Renewing authentication tokens if they are about to expire, enhancing session security and continuity.</li>
 *   <li>Checking user permissions against annotated methods to ensure that the user has the necessary rights to perform the requested operation.</li>
 * </ul>
 *
 * <p>Key methods in this class include:</p>
 * <ul>
 *   <li>{@code filter}: Main method that processes incoming requests and applies authorization logic.</li>
 *   <li>{@code handleTokenRenewal}: Checks and renews the authentication token if necessary.</li>
 *   <li>{@code setupThreadContext}: Configures logging context for the current request.</li>
 *   <li>{@code setSecurityContext}: Sets the security context for the current request, providing user-specific security information.</li>
 *   <li>{@code checkAuthorization}: Verifies if the user has the necessary permissions to access the requested resource.</li>
 * </ul>
 *
 * <p>Exception handling is implemented to ensure that unauthorized or erroneous requests are appropriately managed, returning relevant HTTP responses.</p>
 *
 * <p>This filter also makes use of several custom security annotations to enforce method-level security:</p>
 * <ul>
 *   <li>{@code @RequiresMethodPermission}: This annotation is used to specify that a method requires a particular permission.
 *       The {@code checkAuthorization} method checks if the authenticated user has the required permission before allowing access.</li>
 *   <li>{@code @RequiresProjectRolePermission}: This annotation ensures that the user has a specific role in the project identified by the request.
 *       The filter extracts the project ID from the request and verifies the user's role in the project.</li>
 *   <li>{@code @RequiresProjectMemberPermission}: This annotation ensures that the user is a member of the project identified by the request.
 *       Similar to {@code @RequiresProjectRolePermission}, but only checks for membership, not a specific role.</li>
 *   <li>{@code @RequiresPermissionByUserOnIndividualMessage}: This annotation is used for endpoints dealing with individual messages.
 *       It checks if the user is either the sender or receiver of the message.</li>
 *   <li>{@code @RequiresPermissionByUserOnIndividualMessageAllMessages}: This annotation is used for endpoints dealing with multiple messages.
 *       It ensures that the user has permission to access the messages, typically by checking the user ID.</li>
 * </ul>
 */


@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Logger logger = LogManager.getLogger(AuthorizationFilter.class);

    @EJB
    UserBean userBean;
    @Context
    private ResourceInfo resourceInfo;
    @Context
    private HttpServletRequest request;
    @EJB
    private ConfigurationBean configBean;
    @EJB
    private ProjectMembershipDao projectMembershipDao;
    @EJB
    private SessionBean sessionBean;

    /**
     * Filters incoming requests to enforce authorization rules.
     *
     * <p>This method intercepts incoming HTTPS requests, checks if the requested path is public,
     * validates authentication tokens, renews tokens if necessary, sets up the security context,
     * and verifies user permissions. If the token is invalid or the user does not have the required
     * permissions, the request is aborted with an appropriate HTTPS response status.</p>
     *
     * @param requestContext the context of the incoming request
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String ip = request.getRemoteAddr();
        try {
            if (isPublicEndpoint(path)) {
                // Update ThreadContext with public user details
                setupThreadContext("unknown", ip, "public", "unknown", "unknown");
                return;
            }
            String token = extractTokenFromCookieHeader(request.getHeader("Cookie"));
            if (token == null) {
                abortUnauthorized(requestContext);
                return;
            }
            AuthUserDto authUserDto = sessionBean.validateAuthTokenAndGetUserDetails(token);
            if (authUserDto == null) {
                abortUnauthorized(requestContext);
                return;
            }
            // Update ThreadContext with authenticated user details
            setupThreadContext(String.valueOf(authUserDto.getUserId()), ip, "authenticated", authUserDto.getSessionId().toString(), authUserDto.getUsername());

            //Adding new token to cookie if the token is about to expire
            handleTokenRenewal(token, authUserDto, path, requestContext);

            setSecurityContext(requestContext, authUserDto);
            checkAuthorization(requestContext, authUserDto);
            if(path.contains("/logout")){
                sessionBean.createInvalidSession(authUserDto, requestContext);
            }
        } catch (InvalidCredentialsException e) {
            // Handle UserNotFoundException by responding with an Unauthorized status
            abortUnauthorized(requestContext);
        } catch (Exception e) {
            // Handle other exceptions by responding with an Internal Server Error status
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal Server Error")
                    .build();
            requestContext.abortWith(response);
        }
    }

    /**
     * Handles token renewal if the token is about to expire.
     *
     * <p>This method checks the expiration time of the current token and, if it is about to expire,
     * generates a new token and invalidates the old one to maintain session continuity and security.</p>
     *
     * @param token the authentication token
     * @param authUserDto the authenticated user details
     * @param path the request path
     * @param requestContext the context of the incoming request
     * @throws UserNotFoundException if the user is not found
     * @throws DatabaseOperationException if a database operation fails
     * @throws InputValidationException if there is a validation error
     */
    private  void handleTokenRenewal(String token, AuthUserDto authUserDto, String path, ContainerRequestContext requestContext) throws UserNotFoundException, DatabaseOperationException, InputValidationException {
        Instant now = Instant.now();
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(JwtKeyProvider.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long timeRemaining = expiration.getTime() - now.toEpochMilli();
        long definedTimeOut = configBean.getConfigValueByKey("sessionTimeout");
        long renovateSessionTime = definedTimeOut / GlobalSettings.TIME_OUT_RATIO;
        if (timeRemaining < renovateSessionTime && !path.contains("/logout")) {
            sessionBean.createNewSessionAndInvalidateOld(authUserDto, requestContext, definedTimeOut, token);
        }
    }

    /**
     * Configures the ThreadContext for logging and session tracking.
     *
     * <p>This method sets up the ThreadContext with user-specific details such as user ID, IP address,
     * access level, session ID, and username. These details are used for logging and tracking the user session
     * across requests, providing context for debugging and monitoring purposes.</p>
     *
     * @param userId the ID of the user making the request
     * @param ip the IP address from which the request originated
     * @param access the access level of the request (e.g., "public" or "authenticated")
     * @param sessionId the ID of the user's session
     * @param username the username of the authenticated user
     */
    private void setupThreadContext(String userId,  String ip, String access, String sessionId, String username) {
        ThreadContext.put("userId", userId);
        ThreadContext.put("username", username);
        ThreadContext.put("ip", ip);
        ThreadContext.put("access", access);
        ThreadContext.put("sessionId", sessionId);
    }

    /**
     * Checks if the endpoint is public and does not require authorization.
     *
     * <p>This method evaluates the request path to determine if it corresponds to a public endpoint.
     * Public endpoints are those that do not require user authentication and are accessible to everyone.
     * Examples include login, registration, password reset, and informational endpoints.</p>
     *
     * note: All endpoints using the therm "confirm" are related to email confirmation and have token validation or
     * other security measures
     *
     * @param path the request path
     * @return true if the endpoint is public, false otherwise
     */
    private boolean isPublicEndpoint(String path) {
        return path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/request/password/reset")
                || path.contains("/request/confirmation/email")
                || path.contains("/accept/project")
                || path.contains("/password/reset")
                || path.contains("/labs")
                || path.contains("/projects/all")
                || path.contains("/info/project/states")
                || path.contains("/info/project/roles")
                || path.contains("/enum/roles")
                || path.contains("/enum/states")
                || path.contains("/configurations/all");
    }

    /**
     * Extracts the authentication token from the cookie header.
     *
     * <p>This method parses the "Cookie" header from the HTTPS request to find and extract the value of the "authToken" cookie.
     * If the cookie is present, its value is returned; otherwise, null is returned.</p>
     *
     * @param cookieHeader the "Cookie" header from the HTTPS request
     * @return the authentication token, or null if not found
     */
    public String extractTokenFromCookieHeader(String cookieHeader) {
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("authToken=")) {
                    return cookie.substring("authToken=".length());
                }
            }
        }
        return null;
    }

    /**
     * Sets the security context for the current request.
     *
     * <p>This method configures a custom {@link SecurityContext} for the current request, providing user-specific
     * security information. This context includes details such as the authenticated principal and user roles.</p>
     *
     * <p>The security context methods implemented are:</p>
     * <ul>
     *   <li>{@code getUserPrincipal()}: Returns the authenticated user details as a {@link Principal}.</li>
     *   <li>{@code isUserInRole(String roleId)}: Checks if the user has the specified role.</li>
     *   <li>{@code isSecure()}: Indicates whether the request is secure (currently checking for "http" scheme; should be updated to "https").</li>
     *   <li>{@code getAuthenticationScheme()}: Returns the authentication scheme, which is set to {@code SecurityContext.BASIC_AUTH}.</li>
     * </ul>
     *
     * @param requestContext the context of the incoming request
     * @param authUserDto the authenticated user details
     */
    private void setSecurityContext(ContainerRequestContext requestContext, AuthUserDto authUserDto) {
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return authUserDto;
            }
            @Override
            public boolean isUserInRole(String roleId) {
                return roleId.equals(authUserDto.getRoleId());
            }
            @Override
            public boolean isSecure() {
                //TODO mudar para https
                return requestContext.getUriInfo().getRequestUri().getScheme().equals("http");
            }
            @Override
            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        });
    }

    /**
     * Verifies if the user has the necessary permissions to access the requested resource.
     *
     * <p>This method checks the annotations on the requested method to determine the required permissions and roles.
     * It verifies if the authenticated user has the necessary permissions to proceed with the request. If the user
     * does not have the required permissions, the request is aborted with a FORBIDDEN status.</p>
     *
     * <p>The method handles the following custom security annotations:</p>
     * <ul>
     *   <li>{@code @RequiresMethodPermission}: Checks if the user has the specific permission required by the method.</li>
     *   <li>{@code @RequiresProjectRolePermission}: Ensures the user has the specified role in the project identified by the request.</li>
     *   <li>{@code @RequiresProjectMemberPermission}: Ensures the user is a member of the project identified by the request.</li>
     *   <li>{@code @RequiresPermissionByUserOnIndividualMessage}: Verifies the user is either the sender or receiver of the individual message.</li>
     *   <li>{@code @RequiresPermissionByUserOnIndividualMessageAllMessages}: Verifies the user has permission to access the messages, typically by checking the sender ID.</li>
     * </ul>
     *
     * @param requestContext the context of the incoming request
     * @param authUserDto the authenticated user details
     * @throws InvalidCredentialsException if the user's credentials are invalid
     * @throws IOException if an I/O error occurs
     */
    private void checkAuthorization(ContainerRequestContext requestContext, AuthUserDto authUserDto) throws InvalidCredentialsException, IOException {
        Method method = resourceInfo.getResourceMethod();

        if (method.isAnnotationPresent(RequiresMethodPermission.class)) {
            checkMethodPermission(method, authUserDto, requestContext);
        }
        if (method.isAnnotationPresent(RequiresProjectRolePermission.class)) {
            checkProjectRolePermission(method, authUserDto, requestContext);
        }
        if (method.isAnnotationPresent(RequiresProjectMemberPermission.class)) {
            checkProjectMemberPermission(authUserDto, requestContext);
        }
        if (method.isAnnotationPresent(RequiresPermissionByUserOnIndividualMessage.class)) {
            checkIndividualMessagePermission(requestContext, authUserDto);
        }
        if (method.isAnnotationPresent(RequiresPermissionByUserOnIndividualMessageAllMessages.class)) {
            checkAllMessagesPermission(requestContext, authUserDto);
        }
    }

    private void checkMethodPermission(Method method, AuthUserDto authUserDto, ContainerRequestContext requestContext) {
        long permissionId = method.getAnnotation(RequiresMethodPermission.class).value().getValue();
        boolean hasPermission = authUserDto.getPermissions().stream()
                .anyMatch(methodEntity -> methodEntity.getId() == permissionId);
        if (!hasPermission) {
            logger.error("User does not have permission to access the resource");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private void checkProjectRolePermission(Method method, AuthUserDto authUserDto, ContainerRequestContext requestContext) {
        ProjectRoleEnum requiredRole = method.getAnnotation(RequiresProjectRolePermission.class).value();
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        List<String> projectIdList = pathParams.get("projectId");
        if (projectIdList == null || projectIdList.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity("Project ID is missing").build());
            return;
        }
        long projectId = Long.parseLong(projectIdList.get(0));
        ProjectMembershipEntity membership = projectMembershipDao.findProjectMembershipByUserIdAndProjectId(projectId, authUserDto.getUserId());
        if (membership == null || !membership.getRole().equals(requiredRole)) {
            logger.error("User does not have permission to access the project");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private void checkProjectMemberPermission(AuthUserDto authUserDto, ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        List<String> projectIdList = pathParams.get("projectId");
        if (projectIdList == null || projectIdList.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity("Project ID is missing").build());
            return;
        }
        long projectId = Long.parseLong(projectIdList.get(0));
        ProjectMembershipEntity membership = projectMembershipDao.findProjectMembershipByUserIdAndProjectId(projectId, authUserDto.getUserId());
        if (membership == null) {
            logger.error("User does not have permission to access the project");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private void checkIndividualMessagePermission(ContainerRequestContext requestContext, AuthUserDto authUserDto) {
        MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
        String senderId = pathParams.getFirst("senderId");
        String receiverId = pathParams.getFirst("recipientId");
        boolean hasPermission = senderId.equals(String.valueOf(authUserDto.getUserId())) || receiverId.equals(String.valueOf(authUserDto.getUserId()));
        if (!hasPermission) {
            logger.error("User does not have permission to access the messages");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private void checkAllMessagesPermission(ContainerRequestContext requestContext, AuthUserDto authUserDto) {
        MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
        String senderId = queryParameters.getFirst("userId");
        boolean hasPermission = senderId.equals(String.valueOf(authUserDto.getUserId()));
        if (!hasPermission) {
            logger.error("User does not have permission to access the messages");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    /**
     * Aborts the request with an unauthorized response.
     *
     * <p>This method creates a response with the HTTP status {@code UNAUTHORIZED} (401) and a message indicating that
     * the token is invalid or expired, or that the user does not have the necessary permissions. The request is then
     * aborted with this response.</p>
     *
     * @param requestContext the context of the incoming request
     */
    private void abortUnauthorized(ContainerRequestContext requestContext) {
        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .entity("No Permission, invalid or expired token")
                .build();
        requestContext.abortWith(response);
    }

}
