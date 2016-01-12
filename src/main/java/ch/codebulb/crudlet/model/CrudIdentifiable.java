package ch.codebulb.crudlet.model;

import ch.codebulb.crudlet.service.CrudService;
import ch.codebulb.crudlet.webservice.CrudResource;
import java.io.Serializable;

/**
 * A minimal contract any entity type recognized by {@link CrudService} /
 * {@link CrudResource} must fulfill.
 */
// as in https://github.com/codebulb/crudfaces/blob/master/src/main/java/ch/codebulb/crudfaces/model/CrudIdentifiable.java
public interface CrudIdentifiable extends Serializable {
    Long getId();
    void setId(Long id);
}
