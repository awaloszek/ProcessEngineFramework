package com.bpmnengine.model;

import com.bpmnengine.data.DataContextAccess;
import com.bpmnengine.gen.omg.spec.bpmn.model.TSequenceFlow;

/**
 * @author André Waloszek
 */
public class SequenceFlow extends TSequenceFlow {

    public boolean hasCondition() {
        return getConditionExpression() != null && getConditionExpression() instanceof FormalExpression;
    }

    public boolean evalCondition(DataContextAccess processDataContextAccess) {
        return !hasCondition() || ((FormalExpression) getConditionExpression()).evalCondition(processDataContextAccess);
    }

    @Override
    public boolean isSequenceFlow() {
        return true;
    }

    @Override
    public SequenceFlow asSequenceFlow() {
        return this;
    }
}
