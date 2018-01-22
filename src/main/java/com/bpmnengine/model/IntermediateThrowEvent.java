package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.token.Token;
import com.bpmnengine.gen.omg.spec.bpmn.model.TIntermediateThrowEvent;

import java.util.List;

/**
 * @author André Waloszek
 */
public class IntermediateThrowEvent extends TIntermediateThrowEvent {

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        return super.pass(tokens, processInstance);
    }
}
