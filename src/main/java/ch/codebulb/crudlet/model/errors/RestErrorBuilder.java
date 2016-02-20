package ch.codebulb.crudlet.model.errors;

import ch.codebulb.crudlet.util.JsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Base class for wrappers for constraint violations.
 */
public class RestErrorBuilder {
    protected Throwable exception;
    protected Map responseBody = new HashMap<>();

    public RestErrorBuilder(Throwable exception) {
        this.exception = exception;
        this.responseBody = createResponseBody();
    }
    
    public Response createResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(JsonHelper.build(responseBody)).build();
    }
    
    private Map createResponseBody() {
        Map error = new HashMap();
        
        Map errorValue = new HashMap();
        errorValue.put("exception", exception.getClass().getName());
        errorValue.put("detailMessage", exception.getMessage());
        
        error.put("error", errorValue);
        return error;
    }
}
