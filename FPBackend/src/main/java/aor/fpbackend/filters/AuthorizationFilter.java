package aor.fpbackend.filters;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import aor.fpbackend.bean.UserBean;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.security.Principal;


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

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (isPublicEndpoint(path)) {
            return;
        }
        String token = extractTokenFromCookieHeader(request.getHeader("Cookie"));

        System.out.println("Passou aqui no Authorization Filter: " + token);
        if (token != null) {
            try {
                AuthUserDto authUserDto = userBean.getAuthUserDtoByToken(token);
                if (userBean.tokenValidator(token)) {
                    setSecurityContext(requestContext, authUserDto);
                    checkAuthorization(requestContext, authUserDto);

                } else {
                    abortUnauthorized(requestContext);
                }
            } catch (Exception e) {
                abortUnauthorized(requestContext);
            }
        } else {
            abortUnauthorized(requestContext);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/labs")
                || path.contains("/password/reset");
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
            public boolean isUserInRole(String role) {
                RoleEntity roleEntity = roleDao.findRoleById(authUserDto.getRoleId());
                String userRole = roleEntity.getName().toString();
                return userRole.equalsIgnoreCase(role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
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
            MethodEnum requiredPermissions = method.getAnnotation(RequiresPermission.class).value();
            System.out.println("RoleId: " + authUserDto.getRoleId() + "required Permissions: " + requiredPermissions);
            boolean hasPermission = userBean.isMethodAssociatedWithRole(authUserDto.getRoleId(), requiredPermissions);
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("No Permission, invalid or expired token").build());
    }

}
