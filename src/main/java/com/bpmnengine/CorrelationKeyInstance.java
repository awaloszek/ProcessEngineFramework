package com.bpmnengine;

import com.bpmnengine.gen.omg.spec.bpmn.model.TCorrelationKey;

import java.util.Map;
import java.util.Objects;

/**
 * @author André Waloszek
 */
public class CorrelationKeyInstance {

    private final String id;

    private final Map<String, Object> properties;

    public CorrelationKeyInstance(TCorrelationKey key, Map<String, Object> properties) {
        this.id = key.getId();
        this.properties = properties;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CorrelationKeyInstance))
            return false;

        CorrelationKeyInstance that = (CorrelationKeyInstance) object;

        if (!Objects.equals(this.id, that.id) || !Objects.equals(this.properties.size(), that.properties.size()))
            return false;

        for (Map.Entry<String, Object> property : that.properties.entrySet()) {
            if (!Objects.equals(properties.get(property.getKey()), property.getValue()))
                return false;
        }

        return true;
    }
}
