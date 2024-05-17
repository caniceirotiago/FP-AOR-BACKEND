package aor.fpbackend.filters;

import aor.fpbackend.exception.InvalidCredentialsException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import aor.fpbackend.bean.UserBean;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.function.Function;

@Provider
    @Priority(Priorities.AUTHORIZATION)
    public class AuthorizationFilter implements ContainerRequestFilter {
        @EJB
        UserBean userBean;

        @Context
        private ResourceInfo resourceInfo;
//        @EJB
//        private PermissionDao permissionDao;

        @Override
        public void filter(ContainerRequestContext requestContext) {
            String path = requestContext.getUriInfo().getPath();
            if (path.endsWith("/login")
                    || path.endsWith("/register")
                    || path.contains("/confirm")
                    || path.contains("/request-password-reset")
                    || path.contains("/reset-password")
                    || path.contains("/request-confirmation-email")) {
                return;
            }
            String authHeader = requestContext.getHeaderString("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (userBean.tokenValidator(token)) {
                        checkAuthorization(requestContext, token);
                    } else {
                        abortUnauthorized(requestContext);
                    }
                } catch (InvalidCredentialsException e) {
                    throw new RuntimeException(e);
                }
            } else {
                abortUnauthorized(requestContext);
            }
        }
        private void checkAuthorization(ContainerRequestContext requestContext, String token) {
            Method method = resourceInfo.getResourceMethod();
            if (method.isAnnotationPresent(RequiresPermission.class)) {
              //  Function requiredPermissions = method.getAnnotation(RequiresPermission.class).value();
               // boolean hasPermission = permissionBean.getPermission(token, requiredPermissions);
                //if (!hasPermission) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
               // }
            }
        }

        private void abortUnauthorized(ContainerRequestContext requestContext) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("No Permission, invalid or expired token").build());
        }

}
