package ch.codebulb.crudlet.webservice;

import ch.codebulb.crudlet.config.Options;
import ch.codebulb.crudlet.model.errors.RestValidationConstraintErrorBuilder;
import ch.codebulb.crudlet.model.CrudEntity;
import ch.codebulb.crudlet.model.CrudIdentifiable;
import ch.codebulb.crudlet.model.errors.IllegalRequestExceptions;
import ch.codebulb.crudlet.model.errors.RestErrorBuilder;
import ch.codebulb.crudlet.service.CrudService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * <p>
 * A REST web service endpoint for editing all entities in the persistence storage 
 * including out-of-the-box support for returning I18N-ready model validation error messages.
 * </p>
 * <p>
 * This service realizes these REST operations:</p>
 * <ul>
 * <li><code>GET /contextPath/model</code>: <code>service#findAll()</code>
 * 
 * <ul>
 * <li>Searches for all entities of the given type; or searches for all entities of the given type which match all the given query parameters if the global <code>Options#ALLOW_FILTERS</code> flag is set to <code>true</code>. Allowed filters are:
 * 
 * <ul>
 * <li><code>=</code> String equals, e.g. GET <code>GET /contextPath/customers?city=Los%20Angeles</code></li>
 * <li><code>=&gt;</code> Long greater than or equals, e.g. GET <code>GET /contextPath/customers/1/payments?amount=&gt;100</code></li>
 * <li><code>=&lt;</code> Long less than or equals, e.g. GET <code>GET /contextPath/customers/1/payments?amount=&lt;100</code></li>
 * <li><code>=~</code> String SQL "LIKE", e.g. GET <code>GET /contextPath/customers?address=~%Street</code></li>
 * <li><code>Id=</code> Foreign key equals, e.g. GET <code>GET /contextPath/customers/1/payments?customerId=1</code> (this is rather used programmatically when implementing <code>CrudService</code> class to preconfigure nested service endpoints globally than by actual API clients)</li>
 * </ul></li>
 * <li>returns HTTP 200 OK with list of entities</li>
 * </ul></li>
 * <li><code>GET /contextPath/model/_count</code>: <code>service#countAll()</code>
 * 
 * <ul>
 * <li>Counts all entities of the given type; or counts all entities of the given type which match all the given query parameters if the global <code>Options#ALLOW_FILTERS</code> flag is set to <code>true</code>. Allowed filters are the same as for <code>GET /contextPath/model</code>.</li>
 * <li>returns HTTP 200 OK with the calculation output; or HTTP 403 FORBIDDEN if the global <code>Options#ALLOW_COUNT</code> flag is set to <code>false</code>.</li>
 * </ul></li>
 * <li><code>GET /contextPath/model/:id</code>: <code>service#findById(id)</code>
 * 
 * <ul>
 * <li>Searches for the entity of the given type with the given id.</li>
 * <li>returns HTTP 200 OK with entity if found; or HTTP 404 NOT FOUND if entity is not found.</li>
 * </ul></li>
 * <li><code>POST /contextPath/model</code> with entity: <code>service#save(entity)</code>
 * <ul>
 * <li>Saves the entity for the first time.</li>
 * <li>returns HTTP 200 OK with saved entity (as returned by the insert operation) and <code>Location</code> header with content “/contextPath/model/:id”; or HTTP 400 BAD REQUEST with error information on validation error / if entity's <code>id</code> field is not <code>null</code>.</li>
 * </ul></li>
 * <li><code>PUT /contextPath/model/:id</code> with entity: <code>service#save(entity)</code>
 * 
 * <ul>
 * <li>Updates the existing entity.</li>
 * <li>returns HTTP 200 OK with updated entity (e.g. new id) and <code>Location</code> header with content “/contextPath/model/:id”; or HTTP 400 BAD REQUEST with error information on validation error / if entity's <code>id</code> field is not <code>null</code> nor matches the <code>:id</code> path parameter.</li>
 * </ul></li>
 * <li><code>DELETE /contextPath/model</code>: <code>service#deleteAll()</code>
 * 
 * <ul>
 * <li>Deletes all entities of the given type; or deletes all entities of the given type which match all the given query parameters if the global <code>Options#ALLOW_FILTERS</code> flag is set to <code>true</code>. Allowed filters are the same as for <code>GET /contextPath/model</code>.</li>
 * <li>returns HTTP 204 NO CONTENT; or HTTP 403 FORBIDDEN if the global <code>Options#ALLOW_DELETE_ALL</code> flag is set to <code>false</code>.</li>
 * </ul></li>
 * <li><code>DELETE /contextPath/model/:id</code>: <code>service#delete(id)</code>
 * 
 * <ul>
 * <li>Deletes the entity with the id provided or does nothing if no entity with the id provided exists.</li>
 * <li>returns HTTP 204 NO CONTENT.</li>
 * </ul></li>
 * </ul>
 * <p>
 * In order to create a CRUD REST service endpoint for an entity type, make sure the entity
 * implements {@link CrudIdentifiable} (or inherits from
 * {@link CrudEntity}), create the REST web service endpoint by implementing 
 * <code>CrudResource</code> for the entity and register it as a 
 * <code>&#064;Stateless</code> EJB bean in the container.</p>
 * <p>
 * As an example, a REST service endpoint implementation for a
 * <code>Customer</code> entity can extend <code>CrudResource</code> like this:</p>
 * <pre class="brush:java">
&#064;Path(&quot;customers&quot;)
&#064;Stateless
public class CustomerResource extends CrudResource&lt;Customer&gt; {
    &#064;Inject
    private CustomerService service;

    &#064;Override
    protected CrudService&lt;Customer&gt; getService() {
        return service;
    }
}
                </pre>
 * <ul>
 * <li>The <code>&#064;Path</code> defines the base path of the web 
 * service endpoint.</li>
 * <li>Within the <code>getService()</code> method, return the concrete
 * {@link CrudService} for the entity type in question which you should
 * dependency-inject into the controller.</li>
 * </ul>
 * <p>
 * Now you can use e.g. the httpie command line tool to verify that you 
 * can execute RESTful CRUD operations on your entity running on the 
 * database.</p>
 */
public abstract class CrudResource<T extends CrudIdentifiable> {
    @Context protected UriInfo uri;
    @Context protected SecurityContext context;   
    
    /**
     * Returns a List of all entities.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<T> findAll() {
        if (Options.ALLOW_FILTERS) {
            Map<String, String> queryParameters = getQueryParameters();
            if (!queryParameters.isEmpty()) {
                return new ArrayList<>(findAllEntitiesBy(queryParameters));
            }
        }
        return new ArrayList<>(findAllEntitiesBy(null));
    }

    Map<String, String> getQueryParameters() {
            Map<String, String> ret = new HashMap<>();
        MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
        
        for (Map.Entry<String, List<String>> entrySet : queryParams.entrySet()) {
            ret.put(entrySet.getKey(), entrySet.getValue().get(0));
        }
        return ret;
    }

    
    /**
     * Returns the entity with the {@link CrudEntity#getId()} provided
     * or returns with an error, if none is found.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        final T found = findEntityById(id);
        if (found != null) {
            return Response.status(Response.Status.OK).entity(found).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    /**
     * Counts all entities.
     */
    @GET
    @Path("/_count")
    public Response countAll() {
        if (!Options.ALLOW_COUNT) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        if (Options.ALLOW_FILTERS) {
            Map<String, String> queryParameters = getQueryParameters();
            if (!queryParameters.isEmpty()) {
                return Response.status(Response.Status.OK).entity(countAllEntitiesBy(queryParameters)).build();
            }
        }
        return Response.status(Response.Status.OK).entity(countAllEntitiesBy(null)).build();
    }
    
    /**
     * Inserts the entity provided and returns the updated entity (e.g. updated {@link CrudEntity#getId()} field).<p/>
     * Returns an error if occurred during processing.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(T entity) {
        if (entity != null && entity.getId() != null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return save(entity);
    }
    
    /**
     * Updates the entity provided and returns the updated entity (e.g. updated {@link CrudEntity#getId()} field).<p/>
     * Returns an error if occurred during processing.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, T entity) {
        if (entity.getId() != null && entity.getId() != id) {
            return new RestErrorBuilder(new IllegalRequestExceptions.BodyIdDoesNotMatchPathException()).createResponse();
        }
        entity.setId(id); // enforce id if null
        return save(entity);
    }
    
    /**
     * Deletes all entities.
     */
    @DELETE
    @Path("/")
    public Response deleteAll() {
        if (!Options.ALLOW_DELETE_ALL) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        if (Options.ALLOW_FILTERS) {
            Map<String, String> queryParameters = getQueryParameters();
            if (!queryParameters.isEmpty()) {
                deleteAllEntitiesBy(queryParameters);
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        }
        deleteAllEntitiesBy(null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
       
    /**
     * Deletes the entity with the {@link CrudEntity#getId()} provided.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        deleteEntity(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    /**
     * Returns the service instance. The service should be dependency-injected into this 
     * web service endpoint implementation.
     *
     * @return the service
     */
    protected abstract CrudService<T> getService();
    
    /**
     * Saves the entity provided and returns the respective response.
     */
    private Response save(T entity) {
        boolean create = entity == null || entity.getId() == null;
        try {
            entity = saveEntity(entity);
            return buildSaveReply(entity, create);
        } catch (ConstraintViolationException ex) {
            return new RestValidationConstraintErrorBuilder(ex).createResponse();
        }
    }
    
    /**
     * Calls the service to find all entities which match the queryParameters provided.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected List<T> findAllEntitiesBy(Map<String, String> queryParameters) {
        if (queryParameters == null) {
            return getService().findAll();
        }
        else {
            return getService().findBy(queryParameters);
        }
    }
    
    /**
     * Calls the service to find the entity with the id provided.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected T findEntityById(Long id) {
        return getService().findById(id);
    }
    
    /**
     * Calls the service to count all entities which match the queryParameters provided.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected long countAllEntitiesBy(Map<String, String> queryParameters) {
        if (queryParameters == null) {
            return getService().countAll();
        }
        else {
            return getService().countBy(queryParameters);
        }
    }

    /**
     * Calls the save service with the entity provided.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected T saveEntity(T entity) {
        entity = getService().save(entity);
        return entity;
    }
    
    /**
     * Calls the delete entity service method.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected void deleteEntity(Long id) {
        getService().delete(id);
    }
    
    /**
     * Calls the service to delete all entities which match the queryParameters provided.
     * 
     * Extension point to add custom behavior (e.g. for nested resources).
     */
    protected void deleteAllEntitiesBy(Map<String, String> queryParameters) {
        if (queryParameters == null) {
            getService().deleteAll();
        }
        else {
            getService().deleteBy(queryParameters);
        }
    }

    private Response buildSaveReply(T entity, boolean created) {
        return Response.status(created ? Response.Status.CREATED : Response.Status.OK).entity(entity)
                .header("Location", getRequestBasePath() + "/" + entity.getId()).build();
    }

    /**
     * Gets the request uri without the last appended "id" param, whether that one is present or not.
     */
    String getRequestBasePath() {
        if (!uri.getPathParameters().containsKey("id")) {
            return uri.getPath();
        }
        else {
            StringBuilder sb = new StringBuilder(uri.getPathSegments().get(0).getPath());
            for (PathSegment segment : uri.getPathSegments().subList(1, uri.getPathSegments().size() -1)) {
                sb.append("/");
                sb.append(segment.getPath());
            }
            return sb.toString();
        }
    }
    
    /**
     * Gets the <code>&#064;PathParam</code> with the key provided and casts it in the given type,
     * if necessary.
     *
     * @param <T> the generic type of the parameter in question
     * @param key the key
     * @param type the type
     * @return the path param
     * @throws NumberFormatException signals a casting error
     */
    protected <T> T getPathParam(String key, Class <T> type) throws NumberFormatException {
        String ret = uri.getPathParameters().getFirst(key);
        if (type == Long.class) {
            return (T) Long.valueOf(ret);
        }
        return (T) ret;
    }
}
