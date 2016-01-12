package ch.codebulb.crudlet.model.exceptions;

import ch.codebulb.crudlet.util.JsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

public abstract class RestfulConstraintViolation {
    protected Map responseBody = new HashMap<>();
    
    public Response createResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(JsonHelper.build(responseBody)).build();
    }
}
