package ch.codebulb.crudlet.model.errors;

import ch.codebulb.crudlet.util.JsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * A wrapper for exceptions which transforms them to a REST error response
 * including a response body with a user-friendly, I18N-ready error message.
 */
public class RestErrorBuilder {
    protected Throwable exception;
    protected Map responseBody = new HashMap<>();
    
    public RestErrorBuilder(Throwable exception) {
        this(exception, true);
    }

    protected RestErrorBuilder(Throwable exception, boolean buildDefaultResponseBody) {
        this.exception = exception;
        if (buildDefaultResponseBody) {
            this.responseBody = createResponseBody(exception);
        }
    }
    
    public Response createResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(JsonHelper.build(responseBody)).build();
    }
    
    private static Map createResponseBody(Throwable exception) {
        Map error = new HashMap();
        
        Map errorValue = new HashMap();
        errorValue.put("exception", exception.getClass().getName());
        errorValue.put("detailMessage", exception.getMessage());
        
        error.put("error", errorValue);
        return error;
    }
}
