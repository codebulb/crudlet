package ch.codebulb.crudlet.config;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Contains all the global hooks to modify the framework's default behavior.
 */
public class Options {
    /** Global hook to disable the CORS filter. <code>false</code> means disabled; defaults to <code>true</code>. */
    public static boolean CORS = true;
    
    /** Global hook to disable user-friendly exception output. 
     * If <code>true</code>, details of common runtime exceptions such as 
     * {@link SQLIntegrityConstraintViolationException}s will be returned as the body of
     * a REST error response, which may be a security thread in a production environment;
     * defaults to <code>true</code>. */
    public static boolean RETURN_EXCEPTION_BODY = true;
    
    /** Global hook to disable the <code>DELETE /</code> (delete all) service endpoint. <code>false</code> means disabled; defaults to <code>true</code>. */
    public static boolean SUPPORT_DELETE_ALL = true;
    
    /** Global hook to disable the GET query filter. <code>false</code> means disabled; defaults to <code>true</code>. */
    public static boolean SUPPORT_FILTERS = true;
}
