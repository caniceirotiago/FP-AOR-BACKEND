package aor.fpbackend.filters;


import aor.fpbackend.bean.ConfigurationBean;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
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
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import aor.fpbackend.bean.UserBean;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.security.Principal;
import java.time.Instant;
import java.util.Date;


@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {


    @EJB
    UserBean userBean;
    @EJB
    RoleDao roleDao;

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;
    @EJB
    private UserDao userDao;
    @EJB
    private ConfigurationBean configurationBean;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (isPublicEndpoint(path)) {
            return;
        }
        String token = extractTokenFromCookieHeader(request.getHeader("Cookie"));

        if (token == null) {
            abortUnauthorized(requestContext);
            return;
        }
        try {
            AuthUserDto authUserDto = userBean.validateTokenAndGetUserDetails(token);
            if (authUserDto == null) {
                abortUnauthorized(requestContext);
                return;
            }

            //Adding new token to cookie if the token is about to expire
            Instant now = Instant.now();
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(JwtKeyProvider.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            long timeRemaining = expiration.getTime() - now.toEpochMilli();

            long definedTimeOut = configurationBean.getConfigValueByKey("sessionTimeout");
            long renovateSessionTime = definedTimeOut / 2; //Renovate session if it has less than half of the time remaining
            if (timeRemaining < renovateSessionTime && !path.contains("/logout")) {
                userBean.createNewSessionAndInvalidateOld(authUserDto, requestContext, definedTimeOut, token);
            }
            setSecurityContext(requestContext, authUserDto);
            checkAuthorization(requestContext, authUserDto);
            if(path.contains("/logout")){
                userBean.createInvalidSession(authUserDto, requestContext);
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

    private boolean isPublicEndpoint(String path) {
        return path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/password/reset")
                || path.contains("/labs")
                || path.contains("/projects/all")
                || path.contains("/info/project-states")
                || path.contains("/info/project-roles")
                || path.contains("/project-role")//TODO terá de sair daqui quando houver permissões específicas
                || path.contains("/configurations/all"); //TODO terá de sair daqui quando houver permissões específicas
    }

    public String extractTokenFromCookieHeader(String cookieHeader) {
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("authToken=")) {
                    // Extract the value of the authToken cookie
                    return cookie.substring("authToken=".length());
                }
            }
        }
        return null;
    }

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

    private void checkAuthorization(ContainerRequestContext requestContext, AuthUserDto authUserDto) throws UnknownHostException, InvalidCredentialsException {
        Method method = resourceInfo.getResourceMethod();
        if (method.isAnnotationPresent(RequiresPermission.class)) {
            long permissionId = method.getAnnotation(RequiresPermission.class).value().getValue();
            boolean hasPermission = authUserDto.getPermissions().stream()
                    .anyMatch(methodEntity -> methodEntity.getId() == permissionId);
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .entity("No Permission, invalid or expired token")
                .build();
        requestContext.abortWith(response);
    }

}
