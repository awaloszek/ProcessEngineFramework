package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TEndEvent;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;

import java.util.Collections;
import java.util.List;

/**
 * @author André Waloszek
 */
public class EndEvent extends TEndEvent {

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        if (tokens.size() < 1)
            return Collections.emptyList();

        Token token = tokens.get(0);
        token.setTokenState(TokenState.END);
        return Collections.emptyList();
    }
}
