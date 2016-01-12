package ch.codebulb.crudlet.model.exceptions;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper to transform a {@link SQLIntegrityConstraintViolationException} into a user-friendly
 * response body of a REST error response.
 */
public class RestfulPersistenceIntegrityConstraintViolation extends RestfulConstraintViolation {
    private SQLIntegrityConstraintViolationException cause;

    public RestfulPersistenceIntegrityConstraintViolation(Throwable cause) {
        this.cause = (SQLIntegrityConstraintViolationException) cause;
        this.responseBody = createResponseBody();
    }
    
    private Map createResponseBody() {
        Map error = new HashMap();
        
        Map errorValue = new HashMap();
        errorValue.put("exception", cause.getClass().getName());
        errorValue.put("detailMessage", cause.getMessage());
        
        error.put("error", errorValue);
        return error;
    }
}
