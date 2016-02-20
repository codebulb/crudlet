package ch.codebulb.crudlet.model.errors;

/**
 * Contains all exception classes representing REST contract violations.
 */
public class IllegalRequestExceptions {
    public static class BodyIdIsNotNullException extends Exception {
        public BodyIdIsNotNullException() {
            super("Request body entity's id field is expected to be be null.");
        }
    }
    
    public static class BodyIdDoesNotMatchPathException extends Exception {
        public BodyIdDoesNotMatchPathException() {
            super("Request body entity's id field is expected to be empty or to match id path parameter.");
        }
    }
}
