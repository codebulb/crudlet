package ch.codebulb.crudlet.config;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * A convenience implementation of a CORS allow-all policy "preflight" request filter.
 */
@Provider
@PreMatching
// as in http://www.developerscrappad.com/1781/java/java-ee/rest-jax-rs/java-ee-7-jax-rs-2-0-cors-on-rest-how-to-make-rest-apis-accessible-from-a-different-domain/
public class CorsRequestFilter implements ContainerRequestFilter {
    
    /** Global hook to disable this filter. <code>false</code> means disabled; defaults to <code>true</code>. */
    public static boolean ALLOW_OPTIONS = true;
    
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        // When HttpMethod comes as OPTIONS, just acknowledge that it accepts...
        if (ALLOW_OPTIONS && requestCtx.getRequest().getMethod().equals("OPTIONS")) {
            // Just send a OK signal back to the browser
            requestCtx.abortWith(Response.status(Response.Status.OK).build());
        }
    }
}