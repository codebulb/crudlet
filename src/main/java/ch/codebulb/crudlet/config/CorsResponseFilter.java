package ch.codebulb.crudlet.config;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.PreMatching;

/**
 * A convenience implementation of a CORS allow-all policy response filter.
 */
@Provider
@PreMatching
// as in http://www.developerscrappad.com/1781/java/java-ee/rest-jax-rs/java-ee-7-jax-rs-2-0-cors-on-rest-how-to-make-rest-apis-accessible-from-a-different-domain/
// and http://stackoverflow.com/a/28067653/1399395
public class CorsResponseFilter implements ContainerResponseFilter {
    /** Global hook to disable this filter. <code>false</code> means disabled; defaults to <code>true</code>. */
    public static boolean ALLOW_CORS = true;
 
    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        if (ALLOW_CORS) {
            responseCtx.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseCtx.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseCtx.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseCtx.getHeaders().addAll("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        }
        // Explicitly name all headers used in any Resources
        responseCtx.getHeaders().add("Access-Control-Expose-Headers", "Link");
    }
}