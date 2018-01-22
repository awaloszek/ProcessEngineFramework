package com.bpmnengine;

import java.util.Map;

/**
 * @author André Waloszek
 */
public class ResourceInstance {

    private final String id;

    private final Map<String, Object> properties;

    public ResourceInstance(String id, Map<String, Object> properties) {
        this.id = id;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public Object get(String property) {
        return properties.get(property);
    }
}
