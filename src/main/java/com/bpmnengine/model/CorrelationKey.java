package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TCorrelationKey;
import com.bpmnengine.lang.ListBuilder;

import java.util.List;

import static com.bpmnengine.model.Functions.toStringList;

/**
 * @author André Waloszek
 */
public class CorrelationKey extends TCorrelationKey {

    public List<String> getCorrelationPropertyIds() {
        return ListBuilder.of(getCorrelationPropertyRef()).transform(toStringList()).toList();
    }
}
