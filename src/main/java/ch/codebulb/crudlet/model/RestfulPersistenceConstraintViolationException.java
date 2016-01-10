package ch.codebulb.crudlet.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;

public class RestfulPersistenceConstraintViolationException extends Exception {
    private final Map<String, Map<String, Map<String, Object>>> violationMessages;
    
    public RestfulPersistenceConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        if (violations.size() > 0) {
            Map<String, Map<String, Map<String, Object>>> violationMessages = new HashMap<>();
            for (ConstraintViolation<?> violation : violations) {
                violationMessages.putAll(createConstraintViolationMessage(violation));
            }
            this.violationMessages = violationMessages;
        }
        else {
            this.violationMessages = new HashMap<>();
        }
    }
    
    public Response createResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(build(violationMessages).toString()).build();
    }
    
    private JsonObject build(Map map) {
        JsonObjectBuilder root = Json.createObjectBuilder();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) {
                root.addNull(key.toString());
            }
            else if (value instanceof Map) {
                root.add(key.toString(), build((Map) value));
            }
            else {
                root.add(key.toString(), value.toString());
            }
        }
        return root.build();
    }
    
    private static Map<String, Map<String, Map<String, Object>>> createConstraintViolationMessage(ConstraintViolation violation) {
        Map<String, Map<String, Map<String, Object>>> ret = new HashMap<>();
        Map<String, Map<String, Object>> errors = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        
        String messageTemplate = violation.getMessageTemplate();
        if (messageTemplate != null && messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            messageTemplate = messageTemplate.substring(1, messageTemplate.length()-1);
        }
        info.put("messageTemplate", messageTemplate);
        
        Object invalidValue = violation.getInvalidValue();
        info.put("invalidValue", invalidValue != null ? invalidValue.toString() : null);
        
        info.put("constraintClassName", violation.getConstraintDescriptor().getAnnotation().annotationType().getName());
        
        Map<String, String> attributes = new HashMap<>();
        Map originalAttributes = violation.getConstraintDescriptor().getAttributes();
        for (Object key : originalAttributes.keySet()) {
            if (!(key == null || originalAttributes.get(key) == null || "groups".equals(key) || "message".equals(key) || "payload".equals(key))) {
                attributes.put(key.toString(), originalAttributes.get(key).toString());
            }
        }
        info.put("attributes", attributes);
        
        
        errors.put(violation.getPropertyPath().toString(), info);
        ret.put("errors", errors);
        
        return ret;
    }
}
