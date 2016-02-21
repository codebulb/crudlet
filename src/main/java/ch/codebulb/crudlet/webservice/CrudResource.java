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
 * This service realizes the basic REST operations:</p>
 * <ul>
 * <li><code>GET /contextPath/model</code>: Searches for all entities of the given type</li>
 * <li><code>GET /contextPath/model/:id</code>: Searches for the entities of the given type with the given id</li>
 * <li><code>PUT /contextPath/model/</code> or <code>PUT /contextPath/model/:id</code> or 
 * <code>POST /contextPath/model/</code> or <code>POST /contextPath/model/:id</code>: 
 * Saves the entity for the first time or updates the existing entity, based on the presence of an id on the entity</li>
 * <li><code>DELETE /contextPath/model/:id</code>: Deletes the entity with the id provided</li>
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

    private Map<String, String> getQueryParameters() {
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
        if (entity.getId() != null) {
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
     * Deletes the entity with the {@link CrudEntity#getId()} provided.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        deleteEntity(id);
        return Response.status(Response.Status.NO_CONTENT).build();
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
        boolean create = entity.getId() == null;
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
    private String getRequestBasePath() {
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
