package ch.codebulb.crudlet.service;

import ch.codebulb.crudlet.model.CrudEntity;
import ch.codebulb.crudlet.model.CrudIdentifiable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * An abstract generic base implementation of a CRUD persistence storage access
 * "service" (a "persistence service"), making it easy to derive a
 * best-practices compliant concrete CRUD service implementation.
 * </p>
 * <p>
 * This service realizes the basic CRUD operations:</p>
 * <ul>
 * <li><b>Create (C):</b> <code>create()</code> + <code>save()</code></li>
 * <li><b>Read (R)</b>: <code>findById(Long id)</code> / <code>findAll()</code></li>
 * <li><b>Update (U)</b>: <code>save()</code></li>
 * <li><b>Delete (D)</b>: <code>delete()</code></li>
 * </ul>
 * <p>
 * In order to create a CRUD service for an entity type, make sure the entity
 * implements {@link CrudIdentifiable} (or inherits from
 * {@link CrudEntity}), implement <code>CrudService</code> for the entity
 * and register it as a CDI bean in the container (depending on beans.xml
 * <code>bean-discovery-mode</code>, explicit registration may not be
 * necessary).</p>
 * <p>
 * As an example, a service implementation for a
 * <code>Customer</code> entity can extend <code>CrudService</code> like this:</p>
 * <pre class="brush:java">
public class CustomerService extends CrudService&lt;Customer&gt; {
    &#064;Override
    &#064;PersistenceContext
    protected void setEm(EntityManager em) {
        super.setEm(em);
    }

    &#064;Override
    public Customer create() {
        return new Customer();
    }

    &#064;Override
    public Class&lt;Customer&gt; getModelClass() {
        return Customer.class;
    }
}
                </pre>
 * <ul>
 * <li>Within the {@link #setEm(EntityManager)} method, simply call the
 * super method. The important part is that you inject your
 * <code>@PersistenceContext</code> in this method by annotation.</li>
 * </ul>
 * <p>
 * You can immediately use this service in a backing bean with full support for
 * CRUD operations on your persistence storage.</p>
 *
                <p>
 * Conveniently, Crudlet also comes with an alternative implementation of
 * <code>CrudService</code> named {@link CrudServiceMocked}. As its name
 * suggests, this implementation's "persistence" functionality is based on a
 * simple {@link HashMap} storing the saved entities. Whilst of no use in a
 * real-world production environment, this class might come in handy if you want
 * to try something out without having a proper database / persistence
 * configuration set up. You may then use a {@link CrudServiceMocked}
 * implementation as e.g. a <code>@SessionScoped</code> bean, and later change
 * to a true <code>CrudService</code> without any interface changes.</p>
 */
// as in https://github.com/codebulb/crudfaces/blob/master/src/main/java/ch/codebulb/crudfaces/service/CrudService.java
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
