package ch.codebulb.crudlet.model;

import java.io.Serializable;

public interface CrudIdentifiable extends Serializable {
    Long getId();
    void setId(Long id);
}
