package ch.codebulb.crudlet.util;

import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * A collection of helper utility methods for dealing with JSON.
 */
public class JsonHelper {
    
    /**
     * Builds a {@link JsonObject} from an arbitrarily nested Map.
     * All the leaves of the resulting json object are of type String.
     *
     * @param map the map
     * @return the json object
     */
    public static JsonObject build(Map map) {
        JsonObjectBuilder root = Json.createObjectBuilder();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) {
                root.addNull(key.toString());
            }
            else if (value instanceof Map) {
                root.add(key.toString(), build((Map) value));
            }
            else {
                root.add(key.toString(), value.toString());
            }
        }
        return root.build();
    }
}
