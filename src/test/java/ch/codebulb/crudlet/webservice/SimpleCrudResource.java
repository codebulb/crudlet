package ch.codebulb.crudlet.webservice;

import ch.codebulb.crudlet.SimpleEntity;
import ch.codebulb.crudlet.service.CrudService;
import java.util.HashMap;
import java.util.Map;

public class SimpleCrudResource extends CrudResource<SimpleEntity> {
    private CrudService<SimpleEntity> service;
    
    private Map<String, String> queryParameters = new HashMap<>();

    public SimpleCrudResource(CrudService<SimpleEntity> service) {
        this.service = service;
    }
    @Override
    protected CrudService<SimpleEntity> getService() {
        return service;
    }

    @Override
    // Simply mock queryParameters getter
    Map<String, String> getQueryParameters() {
        return queryParameters;
    }
    
    void addQueryParameter(String key, String value) {
        queryParameters.put(key, value);
    }

    @Override
    // Simply mock requestBasePath getter
    String getRequestBasePath() {
        return "";
    }
}
