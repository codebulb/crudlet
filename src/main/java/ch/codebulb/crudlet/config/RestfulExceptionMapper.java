package ch.codebulb.crudlet.config;

import ch.codebulb.crudlet.model.exceptions.RestfulPersistenceIntegrityConstraintViolation;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider 
public class RestfulExceptionMapper implements ExceptionMapper<Exception> {
    public static boolean RETURN_EXCEPTION_BODY = true;
    
    @Context
    private Providers providers;

    @Override 
    public Response toResponse(Exception ex) {
        if (RETURN_EXCEPTION_BODY) {
            if (ex instanceof RollbackException) {
                Throwable cause = ex.getCause();
                if (cause instanceof PersistenceException) {
                    cause = cause.getCause();
                    if (cause != null) { // The type of this exception is determined at runtime
                        cause = cause.getCause();
                        if (cause instanceof SQLIntegrityConstraintViolationException) {
                            return new RestfulPersistenceIntegrityConstraintViolation(cause).createResponse();
                        }
                    }
                }
            }
        }
        
        ExceptionMapper exceptionMapper = providers.getExceptionMapper(ex.getClass());
        if (exceptionMapper == null) {
            return Response.serverError().build();
        }
        else {
            return exceptionMapper.toResponse(ex);
        }
    }

} 