package ch.codebulb.crudlet.config;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.PreMatching;

@Provider
@PreMatching
// as in http://www.developerscrappad.com/1781/java/java-ee/rest-jax-rs/java-ee-7-jax-rs-2-0-cors-on-rest-how-to-make-rest-apis-accessible-from-a-different-domain/
public class CorsResponseFilter implements ContainerResponseFilter {
 
    public static boolean ALLOW_CORS = true;
 
    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        if (ALLOW_CORS) {
            responseCtx.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseCtx.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseCtx.getHeaders().addAll("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseCtx.getHeaders().add("Access-Control-Max-Age", "1209600");
            responseCtx.getHeaders().addAll("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        }
        // Explicitly name all headers used in any Resources
        responseCtx.getHeaders().addAll("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Link");
    }
}