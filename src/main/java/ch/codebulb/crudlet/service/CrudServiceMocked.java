package ch.codebulb.crudlet.service;

import ch.codebulb.crudlet.model.CrudIdentifiable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CrudServiceMocked<T extends CrudIdentifiable> extends CrudService<T> {  
    private final Map<Long, T> ENTITIES = new HashMap<>();
    private Long currentId = 0L;
    
    @Override
    public T findById(Long id) {
        return ENTITIES.get(id);
    }
    
    @Override
    public List<T> findAll() {
        return new ArrayList<>(ENTITIES.values());
    }
    
    @Override
    public long countAll() {
        return ENTITIES.size();
    }
    
    @Override
    public T save(T entity) {        
        // CREATE
        if (entity.getId() == null) {
            currentId = ++currentId;
            entity.setId(currentId);
        }
        // UPDATE
        ENTITIES.put(entity.getId(), entity);
        
        return entity;
    }
    
    @Override
    public void delete(Long id) {
        ENTITIES.remove(id);
    }
}
