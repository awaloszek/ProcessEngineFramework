package com.bpmnengine.model;

import com.bpmnengine.LifecycleState;
import com.bpmnengine.MessageInstance;
import com.bpmnengine.ProcessInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TStartEvent;
import com.bpmnengine.token.Token;

import java.util.List;

/**
 * @author André Waloszek
 */
public class StartEvent extends TStartEvent {

    @Override
    public boolean isStartEvent() {
        return true;
    }

    @Override
    public StartEvent asStartEvent() {
        return this;
    }

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> tokensMoved = super.pass(tokens, processInstance);
        if (!tokensMoved.isEmpty())
            processInstance.setState(LifecycleState.ACTIVE);
        return tokensMoved;
    }

    @Override
    public boolean correlates(ProcessInstance processInstance, MessageInstance messageInstance) {
        if (processInstance.getState().equals(LifecycleState.ACTIVE))
            return false;

        // no token exists with root in this catch event
        for (Token token : processInstance.getTokens()) {
            if (token.getPath().get(0).equals(getId()))
                return false;
        }

        // no active EventTrigger for this StartEvent exists
        if (processInstance.getCatchEventTriggers(this).size() > 0)
            return false;

        boolean correlates = super.correlates(processInstance, messageInstance);

        if (correlates) {
            Token.create(processInstance, this);
        }

        return correlates;
    }
}
