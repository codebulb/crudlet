package ch.codebulb.crudlet.model.errors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

/**
 * A wrapper to transform a {@link ConstraintViolationException} into a REST error response
 * including a response body with a user-friendly, I18N-ready error message.
 */
public class RestValidationConstraintErrorBuilder extends RestErrorBuilder {
    
    public RestValidationConstraintErrorBuilder(ConstraintViolationException ex) {
        super(ex, false);
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        if (violations.size() > 0) {
            Map violationMessages = new HashMap<>();
            for (ConstraintViolation<?> violation : violations) {
                violationMessages.putAll(createConstraintViolationMessage(violation));
            }
            this.responseBody = violationMessages;
        }
    }
    
    private static Map createConstraintViolationMessage(ConstraintViolation violation) {
        Map ret = new HashMap<>();
        Map validationErrors = new HashMap<>();
        Map info = new HashMap<>();
        
        String messageTemplate = violation.getMessageTemplate();
        if (messageTemplate != null && messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            messageTemplate = messageTemplate.substring(1, messageTemplate.length()-1);
        }
        info.put("messageTemplate", messageTemplate);
        
        Object invalidValue = violation.getInvalidValue();
        info.put("invalidValue", invalidValue != null ? invalidValue.toString() : null);
        
        info.put("constraintClassName", violation.getConstraintDescriptor().getAnnotation().annotationType().getName());
        
        Map attributes = new HashMap<>();
        Map originalAttributes = violation.getConstraintDescriptor().getAttributes();
        for (Object key : originalAttributes.keySet()) {
            if (!(key == null || originalAttributes.get(key) == null || "groups".equals(key) || "message".equals(key) || "payload".equals(key))) {
                attributes.put(key.toString(), originalAttributes.get(key).toString());
            }
        }
        info.put("attributes", attributes);
        
        validationErrors.put(readPropertyName(violation), info);
        ret.put("validationErrors", validationErrors);
        
        return ret;
    }

    private static String readPropertyName(ConstraintViolation violation) {
        Iterator<Path.Node> i = violation.getPropertyPath().iterator();
        Path.Node current = i.next();
        while (i.hasNext()) {
            current = i.next();
        }
        if ("arg0".equals(current.getName())) {
            return ".";
        }
        else {
            return violation.getPropertyPath().toString();
        }
    }
}
