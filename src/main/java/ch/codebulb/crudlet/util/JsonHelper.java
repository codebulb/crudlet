package ch.codebulb.crudlet.util;

import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonHelper {
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
