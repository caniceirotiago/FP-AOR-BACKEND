package aor.fpbackend.filters;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dto.UserDto;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.servlet.http.Cookie;
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

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.function.Function;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    @EJB
    UserBean userBean;
    @EJB
    SessionDao sessionDao;
    @EJB
    RoleDao roleDao;

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/labs")
                || path.contains("/password/reset")) {
            return;
        }
        if (request == null) {
            System.out.println("HttpServletRequest is null");
            abortUnauthorized(requestContext);
            return;
        }
        Cookie[] cookies = request.getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        System.out.println(token);
        if (token != null) {
            try {
                UserDto user = userBean.getUserByToken(token);
                SessionEntity sessionEntity = sessionDao.findSessionByToken(token);

                if (userBean.tokenValidator(token)) {
                    requestContext.setSecurityContext(new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return user;
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            RoleEntity roleEntity = roleDao.findRoleById(user.getRoleId());
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
                    checkAuthorization(requestContext, user);

                } else {
                    abortUnauthorized(requestContext);
                }
            } catch (InvalidCredentialsException | UserNotFoundException e) {
                throw new RuntimeException(e);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            abortUnauthorized(requestContext);
        }
    }

    private void checkAuthorization(ContainerRequestContext requestContext, UserDto user) throws UnknownHostException, InvalidCredentialsException {
        Method method = resourceInfo.getResourceMethod();
        if (method.isAnnotationPresent(RequiresPermission.class)) {
            MethodEnum requiredPermissions = method.getAnnotation(RequiresPermission.class).value();
            boolean hasPermission = userBean.isMethodAssociatedWithRole(user.getRoleId(), requiredPermissions);
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("No Permission, invalid or expired token").build());
    }

}
