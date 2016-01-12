package ch.codebulb.crudlet.model.exceptions;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

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
