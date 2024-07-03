package aor.fpbackend.filters;


import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
/**
 * <p>The {@code ResponseCookieFilter} class is responsible for modifying the HTTPS response to include
 * authentication and session cookies. This is typically used to update the cookies when a new authentication
 * token or session token is generated during the processing of a request.</p>
 *
 * <p>This class is annotated with {@code @Provider} and {@code @Priority(Priorities.HEADER_DECORATOR)} to integrate
 * with the Jakarta REST framework and ensure it is executed at the appropriate phase of the response processing.</p>
 *
 * <p>The main responsibilities of this filter include:</p>
 * <ul>
 *   <li>Extracting new authentication and session tokens from the request context.</li>
 *   <li>Creating new cookies for the authentication and session tokens.</li>
 *   <li>Adding these cookies to the response headers.</li>
 * </ul>
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class ResponseCookieFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo resourceInfo;

    /**
     * Modifies the HTTPS response to include new authentication and session cookies.
     *
     * <p>This method extracts the new authentication and session tokens from the request context properties,
     * creates cookies with these tokens, and adds the cookies to the response headers.</p>
     *
     * @param requestContext the context of the incoming request
     * @param responseContext the context of the outgoing response
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String newAuthToken = (String) requestContext.getProperty("newAuthToken");
        String newSessionToken = (String) requestContext.getProperty("newSessionToken");
        if (newAuthToken != null) {
            NewCookie newAuthCookie = new NewCookie("authToken", newAuthToken, "/", null, "Auth Token", 3600, false, true);
            NewCookie newSessionCookie = new NewCookie("sessionToken", newSessionToken, "/", null, "Session Token", 3600, false, false);
            responseContext.getHeaders().add("Set-Cookie", newAuthCookie.toString());
            responseContext.getHeaders().add("Set-Cookie", newSessionCookie.toString());
        }
    }
}
