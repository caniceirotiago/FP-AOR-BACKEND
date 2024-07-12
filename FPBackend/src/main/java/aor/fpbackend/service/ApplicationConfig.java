package aor.fpbackend.service;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * ApplicationConfig class that configures the base URI path for JAX-RS REST endpoints.
 * The base path for accessing the REST services will be "/rest".
 */
@ApplicationPath("/rest")
public class ApplicationConfig extends Application {

}

