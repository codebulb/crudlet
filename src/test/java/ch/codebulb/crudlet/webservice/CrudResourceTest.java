package ch.codebulb.crudlet.webservice;

import ch.codebulb.crudlet.SimpleEntity;
import ch.codebulb.crudlet.config.Options;
import ch.codebulb.crudlet.model.errors.IllegalRequestExceptions;
import ch.codebulb.crudlet.service.CrudService;
import java.util.Map;
import javax.json.JsonObject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CrudResource} class.<p/>
 * 
 * <b>Note:</b> This does explicitly <i>not</i> test {@link CrudService} functionality, but mocks it.
 */
public class CrudResourceTest {
    private SimpleCrudResource instance;
    private CrudService<SimpleEntity> service;
    
    private static final Long DUMMY_ENTITY_ID = 1l;
    private SimpleEntity dummyEntity;
    
    @Before
    public void init() {
        // Setup application
        Options.ALLOW_FILTERS = true;
        Options.ALLOW_COUNT = true;
        Options.ALLOW_DELETE_ALL = true;
        
        // Setup instance with mocked service
        service = mock(CrudService.class);
        instance = new SimpleCrudResource(service);
        
        // Setup mock service
        when(service.findById(DUMMY_ENTITY_ID)).thenReturn(new SimpleEntity());
        when(service.findById(0l)).thenReturn(null);
        when(service.save(any(SimpleEntity.class))).thenReturn(new SimpleEntity());
        
        // Setup other mocks
        instance.uri = mock(UriInfo.class);
        when(instance.uri.getQueryParameters()).thenReturn(new MultivaluedHashMap<String, String>());
        
        // Setup dummies
        dummyEntity = new SimpleEntity();
    }
    
    @Test
    public void testFindAll() {
        instance.findAll();
        verify(service).findAll();
    }
    
    @Test
    public void testFindAllWithParameters() {
        instance.addQueryParameter("name", "My name");
        instance.findAll();
        verify(service).findBy(instance.getQueryParameters());
    }
    
    @Test
    public void testFindAllWithParametersDisabled() {
        Options.ALLOW_FILTERS = false;
        
        instance.addQueryParameter("name", "My name");
        instance.findAll();
        verify(service).findAll();
    }
    
    @Test
    public void testFindById() {
        Response response = instance.findById(DUMMY_ENTITY_ID);
        verify(service).findById(DUMMY_ENTITY_ID);
        assertEquals(Response.Status.OK, response.getStatusInfo());
        assertNotNull(response.getEntity());
    }
    
    @Test
    public void testFindByIdNotFound() {
        Response response = instance.findById(0l);
        verify(service).findById(0l);
        assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
        assertNull(response.getEntity());
    }
    
    @Test
    public void testCountAll() {
        Response response = instance.countAll();
        verify(service).countAll();
        assertEquals(Response.Status.OK, response.getStatusInfo());
        assertNotNull(response.getEntity());
    }
    
    @Test
    public void testCountAllDisabled() {
        Options.ALLOW_COUNT = false;
        
        Response response = instance.countAll();
        verify(service, never()).countAll();
        verify(service, never()).countBy(any(Map.class));
        assertEquals(Response.Status.FORBIDDEN, response.getStatusInfo());
        assertNull(response.getEntity());
    }
    
    @Test
    public void testCountAllWithParameters() {
        instance.addQueryParameter("name", "My name");
        Response response = instance.countAll();
        verify(service).countBy(instance.getQueryParameters());
        assertEquals(Response.Status.OK, response.getStatusInfo());
        assertNotNull(response.getEntity());
    }
    
    @Test
    public void testCountAllWithParametersDisabled() {
        Options.ALLOW_FILTERS = false;
        
        instance.addQueryParameter("name", "My name");
        instance.countAll();
        verify(service).countAll();
    }
    
    @Test
    public void testAdd() {
        Response response = instance.add(dummyEntity);
        verify(service).save(dummyEntity);
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        assertNotNull(response.getEntity());
        assertNotNull(response.getHeaders().getFirst("Location")); // TODO Test correct Location header response
    }
    
    @Test
    public void testAddIllegal() {
        dummyEntity.setId(1l);
        Response response = instance.add(dummyEntity);
        verify(service, never()).save(any(SimpleEntity.class));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
        assertNull(response.getEntity());
        assertNull(response.getHeaders().get("Location"));
    }
    
    @Test
    public void testUpdate() {
        dummyEntity.setId(1l);
        Response response = instance.update(1l, dummyEntity);
        verify(service).save(dummyEntity);
        assertEquals(Response.Status.OK, response.getStatusInfo());
        assertNotNull(response.getEntity());
        assertNotNull(response.getHeaders().getFirst("Location")); // TODO Test correct Location header response
    }
    
    @Test
    public void testUpdateWithoutBody() {
        // with empty body
        Response response = instance.update(1l, dummyEntity);
        verify(service).save(dummyEntity);
        assertEquals(Response.Status.OK, response.getStatusInfo());
        assertNotNull(response.getEntity());
        assertNotNull(response.getHeaders().getFirst("Location")); // TODO Test correct Location header response
    }
    
    @Test
    public void testUpdateIllegal() {
        dummyEntity.setId(2l);
        Response response = instance.update(1l, dummyEntity);
        verify(service, never()).save(any(SimpleEntity.class));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
        assertNotNull(response.getEntity()); // TODO Test correct error response
        assertNull(response.getHeaders().get("Location"));
    }
    
    @Test
    public void testDeleteAll() {
        Response response = instance.deleteAll();
        verify(service).deleteAll();
        assertEquals(Response.Status.NO_CONTENT, response.getStatusInfo());
        assertNull(response.getEntity());
    }
    
    @Test
    public void testDeleteDisabled() {
        Options.ALLOW_DELETE_ALL = false;
        
        Response response = instance.deleteAll();
        verify(service, never()).deleteAll();
        verify(service, never()).deleteBy(any(Map.class));
        assertEquals(Response.Status.FORBIDDEN, response.getStatusInfo());
        assertNull(response.getEntity());
    }
    
    @Test
    public void testDeleteAllWithParameters() {
        instance.addQueryParameter("name", "My name");
        Response response = instance.deleteAll();
        verify(service).deleteBy(instance.getQueryParameters());
        assertEquals(Response.Status.NO_CONTENT, response.getStatusInfo());
        assertNull(response.getEntity());
    }
    
    @Test
    public void testDeleteAllWithParametersDisabled() {
        Options.ALLOW_FILTERS = false;
        
        instance.addQueryParameter("name", "My name");
        instance.deleteAll();
        verify(service).deleteAll();
    }
}
