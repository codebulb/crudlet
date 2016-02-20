package ch.codebulb.crudlet.model.errors;

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
