package ch.codebulb.crudlet.service;

import ch.codebulb.crudlet.model.CrudIdentifiable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A mocked {@link CrudService} implementation which stores entities in a
 * {@link HashMap} rather than persisting them in an actual persistence
 * storage.</p>
 *
 * <p>
 * Whilst of no use in a real-world production environment, this class might
 * come in handy if you want to try something out without having a proper
 * database / persistence configuration set up. You may then use a
 * <code>CrudServiceMocked</code> implementation as e.g. a
 * <code>@SessionScoped</code> bean, and later change to a true
 * {@link CrudService} without any interface changes.</p>
 */
// as in https://github.com/codebulb/crudfaces/blob/master/src/main/java/ch/codebulb/crudfaces/service/CrudServiceMocked.java
// TODO Add cascading support
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

    @Override
    public List<T> findBy(Map<String, String> predicates) {
        throw new UnsupportedOperationException("Custom filtering not implemented.");
    }

    @Override
    public long countBy(Map<String, String> predicates) {
        throw new UnsupportedOperationException("Custom filtering not implemented.");
    }
}
