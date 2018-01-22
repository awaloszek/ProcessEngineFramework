package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowElement;

/**
 * @author André Waloszek
 */
public abstract class FlowElement extends TFlowElement {

    public boolean isSequenceFlow() {
        return false;
    }

    public SequenceFlow asSequenceFlow() {
        throw new IllegalStateException("This is not of type SequenceFlow");
    }
}
