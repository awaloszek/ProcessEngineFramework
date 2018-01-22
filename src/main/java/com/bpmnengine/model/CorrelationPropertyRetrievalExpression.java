package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TCorrelationPropertyRetrievalExpression;

/**
 * @author André Waloszek
 */
public class CorrelationPropertyRetrievalExpression extends TCorrelationPropertyRetrievalExpression {

    public String getMessageId() {
        return getMessageRef().toString();
    }
}
