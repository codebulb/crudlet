package ch.codebulb.crudlet.webservice;

import ch.codebulb.crudlet.model.RestfulPersistenceConstraintViolationException;
import ch.codebulb.crudlet.model.CrudIdentifiable;
import ch.codebulb.crudlet.service.CrudService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

public abstract class CrudResource<T extends CrudIdentifiable> {
    @Context protected UriInfo uri;
    @Context protected SecurityContext context;   
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<T> findAll() {
        return new ArrayList<>(getService().findAll());
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        final T found = getService().findById(id);
        if (found != null) {
            return Response.status(Response.Status.OK).entity(found).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(T entity) {
        try {
            entity = getService().save(entity);
            return Response.status(Response.Status.OK).entity(entity).header("Link", buildLinkMap(uri.getPath(), entity.getId())).build();
        } catch (RestfulPersistenceConstraintViolationException ex) {
            return ex.createResponse();
        }
    }
    
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveWithId(T entity) {
        return save(entity);
    }
    
    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(T entity) {
        return save(entity);
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addWithId(T entity) {
        return save(entity);
    }
    
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        getService().delete(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteEntity(@PathParam("id") Long id, T entity) {
        return delete(id);
    }
    
    protected abstract CrudService<T> getService();
    
    protected <T> T getPathParam(String key, Class <T> type) throws NumberFormatException {
        String ret = uri.getPathParameters().getFirst(key);
        if (type == Long.class) {
            return (T) Long.valueOf(ret);
        }
        return (T) ret;
    }
    
    private static Map<String, String> buildLinkMap(String uriPath, Long id) {
        Map<String, String> ret = new HashMap<>();
        ret.put("edit", uriPath + "/" + id);
        return ret;
    }
}
