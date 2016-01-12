package ch.codebulb.crudlet.service;

import ch.codebulb.crudlet.model.CrudEntity;
import ch.codebulb.crudlet.model.CrudIdentifiable;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

@Transactional(Transactional.TxType.REQUIRED)
public abstract class CrudService<T extends CrudIdentifiable> implements Serializable {
    protected EntityManager em;
    
    /**
     * Invokes the constructor for the entity type.
     */
    public abstract T create();
    
    /**
     * Returns the entity type.
     */
    public abstract Class<T> getModelClass();
    
    /**
     * Sets the entity manager. Override this method to dependency-inject an {@link EntityManager}
     * with the associated <code>@PersistenceContext</code>
     */
    protected void setEm(EntityManager em) {
        this.em = em;
    }
    
    /**
     * Returns the entity with the {@link CrudEntity#getId()} provided.
     */
    public T findById(Long id) {
        return em.find(getModelClass(), id);
    }
    
    /**
     * Returns a List of all entities.
     */
    public List<T> findAll() {
        CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(getModelClass());
        query.select(query.from(getModelClass()));
        return (List<T>) em.createQuery(query).getResultList();
    }
    
    /**
     * Counts the number of entities.
     */
    public long countAll() {
        CriteriaQuery<Long> query = em.getCriteriaBuilder().createQuery(Long.class);
        query.select(em.getCriteriaBuilder().count(query.from(getModelClass())));
        return em.createQuery(query).getSingleResult();
    }
    
    /**
     * Saves / Inserts / Updates the entity provided and returns the updated entity (e.g. updated {@link CrudEntity#getId()} field.<p/>
     * <b>Note:</b> It's important to continue to work with the newly returned, updated entity rather than with the original entity.
     */
    public T save(@NotNull T entity) {        
        
            if (entity.getId() == null) {
                    em.persist(entity);
            }
            else {
                entity = em.merge(entity);
            }
            em.flush();
            return entity;
        
    }
    
    /**
     * Deletes the entity with the {@link CrudEntity#getId()} provided.
     */
    public void delete(Long id) {
        T entity = em.getReference(getModelClass(), id);
        em.remove(entity);
    }
}
