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

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class ResponseCookieFilter implements ContainerResponseFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String newAuthToken = (String) requestContext.getProperty("newAuthToken");
        if (newAuthToken != null) {
            NewCookie newAuthCookie = new NewCookie("authToken", newAuthToken, "/", null, "Auth Token", 3600, false, true);
            responseContext.getHeaders().add("Set-Cookie", newAuthCookie.toString());
        }
    }
}
